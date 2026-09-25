package com.example.orders.acceptance.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "consumed_events")
public class ConsumedEvent {
    @Id
    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(name = "event_type", length = 64, nullable = false)
    private String eventType;

    @Column(name = "consumed_at", nullable = false)
    private Instant consumedAt;

    protected ConsumedEvent() { }

    public ConsumedEvent(String eventId, String eventType, Instant consumedAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.consumedAt = consumedAt;
    }
}
