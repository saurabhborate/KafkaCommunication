package com.example.orders.acceptance.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    public enum State { PENDING, PUBLISHED }

    @Id
    @Column(length = 64, nullable = false)
    private String id;

    @Column(name = "aggregate_id", length = 64, nullable = false)
    private String aggregateId;

    @Column(name = "event_type", length = 64, nullable = false)
    private String eventType;

    @Column(length = 32000, nullable = false)
    private String payload;

    @Column(name = "correlation_id", length = 128, nullable = false)
    private String correlationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private State state;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected OutboxEvent() { }

    public OutboxEvent(String id, String aggregateId, String eventType, String payload,
                       String correlationId, Instant createdAt) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
        this.state = State.PENDING;
    }

    public void markPublished(Instant now) { state = State.PUBLISHED; publishedAt = now; lastError = null; }
    public void markRetry(String error) {
        retryCount++;
        lastError = error == null ? "unknown publish error" : error.substring(0, Math.min(1000, error.length()));
    }
    public String getId() { return id; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public String getCorrelationId() { return correlationId; }
    public int getRetryCount() { return retryCount; }
}
