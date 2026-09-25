#!/usr/bin/env bash
set -Eeuo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE=(docker compose -f "$ROOT/docker/docker-compose.yml")
ACCEPTANCE="${ACCEPTANCE_URL:-http://localhost:18181}"
NOTIFICATION="${NOTIFICATION_URL:-http://localhost:18182}"
ORDER_SERVICE="${ORDER_SERVICE_URL:-http://localhost:18183}"
order_id=""
fail(){ printf '\n========================================\nE2E TEST FAILED\n========================================\n%s\n' "$*"; "${COMPOSE[@]}" logs --tail=100 || true; exit 1; }
docker info >/dev/null 2>&1 || fail 'Docker is not running. Start Docker Desktop, then retry.'
"${COMPOSE[@]}" up --build -d --wait || fail 'Docker Compose failed to start Kafka/services.'
for attempt in $(seq 1 60); do curl -fsS "$ACCEPTANCE/actuator/health" >/dev/null 2>&1 && curl -fsS "$ORDER_SERVICE/actuator/health" >/dev/null 2>&1 && curl -fsS "$NOTIFICATION/actuator/health" >/dev/null 2>&1 && break; sleep 2; done
curl -fsS "$ACCEPTANCE/actuator/health" >/dev/null || fail 'Acceptance Service is not healthy.'
curl -fsS "$ORDER_SERVICE/actuator/health" >/dev/null || fail 'Order Service is not healthy.'
curl -fsS "$NOTIFICATION/actuator/health" >/dev/null || fail 'Notification Service is not healthy.'
response="$(curl -fsS -H 'Content-Type: application/json' -H "X-Correlation-ID: e2e-$(date +%s)" -d '{"customerId":"CUST-E2E","product":"MacBook Pro","quantity":1,"amount":1500.00}' "$ACCEPTANCE/api/orders")" || fail 'POST /api/orders failed.'
order_id="$(python3 -c 'import json,sys; print(json.load(sys.stdin)["orderId"])' <<<"$response")" || fail "Could not parse orderId from: $response"
order='{}'
for attempt in $(seq 1 60); do
  order="$(curl -fsS "$ACCEPTANCE/api/orders/$order_id" 2>/dev/null || true)"
  [[ -n "$order" ]] || order='{}'
  status="$(python3 -c 'import json,sys; print(json.load(sys.stdin).get("status",""))' <<<"$order" 2>/dev/null || true)"
  [[ "$status" == PROCESSED ]] && break
  sleep 2
done
[[ -n "$order" ]] || fail "Acceptance Service did not return order $order_id."
[[ "$status" == PROCESSED ]] || fail "Expected PROCESSED, received status '$status': $order"
notification=""
for attempt in $(seq 1 60); do
  notification="$(curl -fsS "$NOTIFICATION/api/notifications/$order_id" 2>/dev/null || true)"
  [[ -n "$notification" ]] && break
  sleep 2
done
[[ -n "$notification" ]] || fail "Notification Service did not create a record for $order_id."
payment="$(python3 -c 'import json,sys; print(json.load(sys.stdin).get("paymentStatus",""))' <<<"$notification")"
[[ "$payment" == PAYMENT_COMPLETED ]] || fail "Expected PAYMENT_COMPLETED, received '$payment': $notification"
printf 'Order: %s\nStatus: %s\nPayment: %s\n' "$order_id" "$status" "$payment"
printf '\n========================================\nE2E TEST PASSED\n========================================\n'
