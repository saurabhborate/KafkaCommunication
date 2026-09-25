package com.example.orders.contracts;

import java.math.BigDecimal;
import java.time.Instant;

/** Versioned integration event emitted after an accepted order and its outbox row commit. */
public record OrderAcceptedEvent(
        String eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String correlationId,
        String orderId,
        String customerId,
        String product,
        int quantity,
        BigDecimal amount) {

    public static final String TYPE = "ORDER_ACCEPTED";
    public static final int VERSION = 1;
}
