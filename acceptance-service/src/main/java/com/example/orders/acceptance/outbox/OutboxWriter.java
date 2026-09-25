package com.example.orders.acceptance.outbox;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OutboxWriter {
    private final OutboxEventRepository repository;

    public OutboxWriter(OutboxEventRepository repository) { this.repository = repository; }

    public void append(String aggregateId, String eventType, String payload,
                       String correlationId, Instant createdAt) {
        repository.save(new OutboxEvent(UUID.randomUUID().toString(), aggregateId, eventType,
                payload, correlationId, createdAt));
    }
}
