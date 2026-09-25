package com.example.orders.contracts;

import java.math.BigDecimal;
import java.time.Instant;

/** Versioned integration event emitted when order processing commits successfully. */
public record OrderProcessedEvent(
        String eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String correlationId,
        String orderId,
        String customerId,
        String product,
        int quantity,
        BigDecimal amount,
        String processingStatus) {

    public static final String TYPE = "ORDER_PROCESSED";
    public static final int VERSION = 1;
    public static final String PROCESSED = "PROCESSED";
}
