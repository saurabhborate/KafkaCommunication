# HTTP API

Base URLs: Acceptance `http://localhost:18181`; Notification `http://localhost:18182`. Order Service has no public business API; its health endpoint is on port 18183.

## Create order

`POST /api/orders` returns HTTP `202 Accepted`. `customerId`, `product`, positive `quantity` and positive `amount` are required. Optional `X-Correlation-ID` is propagated to events; if omitted, the service generates one.

```json
{"customerId":"CUST-1001","product":"MacBook Pro","quantity":1,"amount":1500.00}
```

Example accepted response:

```json
{"orderId":"ORD-<uuid>","customerId":"CUST-1001","product":"MacBook Pro","quantity":1,"amount":1500.00,"status":"ACCEPTED","createdAt":"2026-09-24T10:00:00Z","updatedAt":"2026-09-24T10:00:00Z"}
```

## Get order

`GET /api/orders/{orderId}` is served by Acceptance Service. It returns `200` with acceptance-owned order data and status `ACCEPTED` or `PROCESSED`, or `404` if absent.

## Get notification

`GET /api/notifications/{orderId}` returns the notification/payment record or `404` until the processed event is handled. `GET /api/notifications` returns all notifications (local demo convenience).

## Health and OpenAPI

All services expose `/actuator/health`; the Acceptance and Notification services also expose `/swagger-ui/index.html` and `/v3/api-docs`.
