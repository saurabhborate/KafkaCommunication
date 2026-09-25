# Architecture

Three independently deployable Spring Boot 4.1.1 services use Apache Kafka 4.3.1 in KRaft mode. Services communicate only through Kafka in the order workflow. Each service owns a separate H2 file database and is the only process that reads or writes it.

```mermaid
flowchart LR
  C[Client] -->|POST /api/orders| A[Acceptance Service\nacceptance_db]
  A -->|same DB transaction: order + outbox| AO[(Acceptance Outbox)]
  AO -->|ORDER_ACCEPTED, key=orderId| KA[(orders.accepted)]
  KA --> O[Order Service\norder_db]
  O -->|same DB transaction: processing + outbox| OO[(Order Outbox)]
  OO -->|ORDER_PROCESSED, key=orderId| KP[(orders.processed)]
  KP -->|acceptance-service-group| A
  KP -->|notification-service-group| N[Notification Service\nnotification_db]
  A -->|GET /api/orders/{orderId}| C
  N -->|GET /api/notifications/{orderId}| C
  KA -. retry exhausted .-> ADLQ[(orders.accepted.dlq)]
  KP -. retry exhausted .-> PDLQ[(orders.processed.dlq)]
```

## Message processing and consistency

Acceptance stores the accepted order and `ORDER_ACCEPTED` outbox record atomically. A scheduled publisher sends pending records to Kafka and marks each published after broker acknowledgement. Order Service stores `RECEIVED`, moves it to `PROCESSED`, records the input event ID, and writes its `ORDER_PROCESSED` outbox row in one local transaction. The event is emitted only after that transaction has committed. Consumers acknowledge Kafka records after their local transaction commits.

The outbox publisher is at-least-once: a process crash after Kafka acknowledges but before the outbox is marked published can cause a duplicate. Each consumer stores consumed event IDs under a unique primary key and has unique business constraints, so repeats do not repeat side effects. Cross-service DB/Kafka atomic transactions are not claimed.

## Failure handling

Kafka listener failures retry twice after the initial delivery, with one second between retries. After those retries, Spring Kafka's `DeadLetterPublishingRecoverer` publishes the record to the matching topic plus `.dlq`, retaining the original key and headers. For example, an exhausted `orders.accepted` record goes to `orders.accepted.dlq`. Invalid messages are not silently discarded. Monitor DLQ topics and replay only after correcting the cause.

Outbox publish failures keep the record `PENDING`, increment `retry_count`, store a bounded error, and retry on later polls. This supports recovery after Kafka returns. This local example deliberately uses one Kafka broker and replication factor 1; it demonstrates application reliability patterns, not multi-node broker availability or a full operations platform.
