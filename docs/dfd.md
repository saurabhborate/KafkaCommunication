# Data Flow Diagram

```mermaid
flowchart TD
  Client[Client]
  Accept[Acceptance Service]
  ADB[(acceptance_db\norders, outbox_events, consumed_events)]
  Accepted[(Kafka: orders.accepted)]
  Process[Order Service]
  ODB[(order_db\norder_processing, outbox_events, consumed_events)]
  Processed[(Kafka: orders.processed)]
  Notify[Notification Service]
  NDB[(notification_db\nnotifications, consumed_events)]
  ADead[(orders.accepted.dlq)]
  PDead[(orders.processed.dlq)]
  Client -->|POST order| Accept
  Accept -->|insert order and event atomically| ADB
  ADB -->|outbox publisher| Accepted
  Accepted -->|consumer group: order-service-group| Process
  Process -->|receive, process, persist outbox atomically| ODB
  ODB -->|outbox publisher| Processed
  Processed -->|consumer group: acceptance-service-group| Accept
  Accept -->|update its own order status| ADB
  Processed -->|consumer group: notification-service-group| Notify
  Notify -->|payment decision and dedup record| NDB
  Client -->|GET /api/orders/{orderId}| Accept
  Accept -->|read own order| ADB
  Client -->|GET /api/notifications/{orderId}| Notify
  Notify -->|read own notification| NDB
  Accepted -. after retry exhaustion .-> ADead
  Processed -. after retry exhaustion .-> PDead
```

Kafka transports versioned JSON events. The `orderId` is the Kafka key, preserving per-order partition ordering. Acceptance and Notification use separate consumer groups for `orders.processed`, so both receive every processed event independently.
