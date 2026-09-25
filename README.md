# Kafka Order Processing

A locally runnable event-driven order workflow built as three independent Spring Boot services with Apache Kafka in KRaft mode. Acceptance owns accepted orders; Order owns processing records; Notification owns payment/notification records. Their databases are private to each service.

## Architecture

```mermaid
flowchart LR
 C[Client] -->|POST order| A[Acceptance Service]
 A -->|order + outbox in one DB tx| ADB[(acceptance_db)]
 ADB -->|ORDER_ACCEPTED| T1[(orders.accepted)]
 T1 --> O[Order Service]
 O -->|processing + outbox in one DB tx| ODB[(order_db)]
 ODB -->|ORDER_PROCESSED| T2[(orders.processed)]
 T2 -->|acceptance-service-group| A
 T2 -->|notification-service-group| N[Notification Service]
 A -->|GET /api/orders/{id}| C
 N -->|GET /api/notifications/{id}| C
 T1 -. exhausted retries .-> D1[(orders.accepted.dlq)]
 T2 -. exhausted retries .-> D2[(orders.processed.dlq)]
```

See [architecture](docs/architecture.md), [data flow](docs/dfd.md) and [API reference](docs/api.md).

## Components

| Service | Port | Database | Responsibility |
|---|---:|---|---|
| Acceptance Service | 18181 | `acceptance_db` | Validate/accept orders, publish accepted events through outbox, consume processed events, serve order reads |
| Order Service | 18183 | `order_db` | Consume accepted events, record processing, publish processed events through outbox |
| Notification Service | 18182 | `notification_db` | Consume processed events, deterministically record payment status, serve notification reads |
| Kafka | 29192 external / 9092 internal | Kafka named volume | KRaft broker, 3 partitions per topic, one replica for local development |

**Topic and group names:** `orders.accepted` (`order-service-group`); `orders.processed` (`acceptance-service-group` and `notification-service-group`); retry exhaustion topics `orders.accepted.dlq` and `orders.processed.dlq`. Kafka message keys are `orderId`; event JSON includes `eventId`, `eventType`, `eventVersion`, `occurredAt`, `correlationId`, and order fields. Payload definitions live in `event-contracts`.

## Data ownership and reliability

Each H2 database is a separate named Docker volume and is never accessed by another service. Each application schema is created and validated on startup.

The Acceptance and Order services use the **Transactional Outbox** pattern. The business row and event payload are inserted in one local database transaction. A scheduled publisher sends pending outbox rows and marks them published only after Kafka acknowledges. A crash between broker acknowledgement and marking can produce duplicates; consumers therefore persist event IDs with unique keys and process idempotently. Notification also has a unique event ID constraint. This is at-least-once delivery, not a distributed DB/Kafka transaction.

Kafka listener processing retries twice, one second apart, after the initial attempt. An event that still fails goes to the matching `.dlq` topic. Outbox publish failures remain pending and retry in later polling cycles. A local one-broker Kafka setup with replication factor 1 is useful for development and does not provide broker high availability.

The example payment rule is deterministic: amount `<= 5000.00` gives `PAYMENT_COMPLETED`; greater amounts give `PAYMENT_PENDING`.

## Prerequisites and versions

Versions selected for this implementation: Java 26, Spring Boot 4.1.1, Apache Kafka 4.3.1, Maven 3.9+, Docker Desktop with Docker Compose v2. The containers use Java 26. Host Java and Maven are only needed for running Maven commands outside Docker. Kafka and all three applications run in Docker Compose.

Check prerequisites:

```bash
java -version
mvn -version
docker version
docker compose version
```

## Clone from GitHub

After the GitHub repository has been created, clone its HTTPS or SSH URL:

```bash
git clone https://github.com/<OWNER>/<REPOSITORY>.git
cd <REPOSITORY>
```

Then run the build and start commands below from that directory. The repository contains the complete Maven reactor, Docker build files, schemas, scripts, and docs; no generated jars or local databases are required.

On macOS with Homebrew, install Java and Maven if needed:

```bash
brew install openjdk maven
```

Then start Docker Desktop. If your shell needs Homebrew's Java symlink, use the exact caveat printed by `brew info openjdk`; `openjdk@26` may not be a valid formula name on every Homebrew snapshot.

## Run the full stack

From the project root:

```bash
bash scripts/start.sh
```

This builds all three service images, starts Kafka, waits for health checks, and prints the service URLs. Alternatively:

```bash
docker compose -f docker/docker-compose.yml up --build -d --wait
docker compose -f docker/docker-compose.yml ps
```

The Compose command starts Kafka and all three Spring Boot services together. For service-specific status and logs:

```bash
docker compose -f docker/docker-compose.yml ps
docker compose -f docker/docker-compose.yml logs -f acceptance-service
docker compose -f docker/docker-compose.yml logs -f order-service
docker compose -f docker/docker-compose.yml logs -f notification-service
```

To stop containers and preserve local data:

```bash
bash scripts/stop.sh
```

To erase Kafka and all service data, explicitly run `docker compose -f docker/docker-compose.yml down -v`.

## Exercise the flow

Create an order:

```bash
curl -i -X POST http://localhost:18181/api/orders \
  -H 'Content-Type: application/json' \
  -H 'X-Correlation-ID: demo-request-001' \
  -d '{"customerId":"CUST-1001","product":"MacBook Pro","quantity":1,"amount":1500.00}'
```

Copy `orderId` from the `202 Accepted` response, then poll the acceptance-owned read API until status is `PROCESSED`:

```bash
curl -i http://localhost:18181/api/orders/ORD-<uuid>
```

Read payment/notification data:

```bash
curl -i http://localhost:18182/api/notifications/ORD-<uuid>
```

Try an amount above 5000 to see `PAYMENT_PENDING`. Invalid requests receive `400`. Health URLs are `http://localhost:18181/actuator/health`, `http://localhost:18183/actuator/health`, and `http://localhost:18182/actuator/health`. Swagger UI is available at `http://localhost:18181/swagger-ui/index.html` and `http://localhost:18182/swagger-ui/index.html`.

## Build and tests

```bash
mvn clean verify
```

Unit tests cover acceptance status synchronization/idempotency, order processing/outbox creation/duplicate accepted events, and completed/pending payment plus duplicate processed events. The Order Service includes Testcontainers Kafka test support for broker-backed integration tests. Docker must be reachable to run Testcontainers tests.

## End-to-end verification

```bash
bash scripts/e2e-test.sh
```

The script verifies Docker, builds/starts the stack, creates an order, waits for Acceptance to report `PROCESSED`, fetches the notification, checks the expected payment result, and prints `E2E TEST PASSED` or diagnostics and `E2E TEST FAILED`. It leaves services running for inspection.

## Troubleshooting

- **Docker daemon/socket unavailable:** Start Docker Desktop and confirm `docker info` succeeds.
- **Port already in use:** Free ports 18181, 18182, 18183, or 29192, or change the host-side port mappings in `docker/docker-compose.yml`.
- **Service is unhealthy:** Inspect `docker compose -f docker/docker-compose.yml logs --tail=200 acceptance-service order-service notification-service kafka`.
- **Order remains ACCEPTED:** Check Order Service logs, Kafka health, and `orders.accepted`; inspect the acceptance outbox state/retry count using the H2 console or local DB tooling.
- **Notification does not appear:** Check Notification logs and the `orders.processed` / `orders.processed.dlq` topics.
- **DLQ records:** The consumer exhausted the configured retry attempts. Correct the malformed payload or processing cause before replaying; replay is an operator action.
- **Reset local data:** `docker compose -f docker/docker-compose.yml down -v` deletes all persisted local databases and Kafka data.

## Project layout

```text
pom.xml
event-contracts/            Shared versioned event records
acceptance-service/         HTTP API, acceptance DB, outbox, processed consumer
order-service/              Processing DB, accepted consumer, processed-event outbox
notification-service/       Notification DB, processed consumer, query API
docker/docker-compose.yml   KRaft Kafka and three application containers
scripts/                    Start, stop and E2E verification
docs/                       Architecture, DFD and API descriptions
```

## Git workflow

For changes after cloning, create a topic branch, review the diff, commit, and push:

```bash
git switch -c docs/my-change
git status
git diff
git add README.md
git commit -m "docs: describe local setup"
git push -u origin docs/my-change
```

Merge through your repository's normal review process, then update the local main branch with `git switch main` and `git pull --ff-only`.
