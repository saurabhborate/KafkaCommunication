package com.example.orders.processing.outbox;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="outbox_events", indexes=@Index(name="idx_outbox_state_created", columnList="state,created_at"))
public class OutboxEvent {
    public enum State { PENDING, PUBLISHED }
    @Id @Column(length=64) private String id;
    @Column(name="aggregate_id", nullable=false, length=64) private String aggregateId;
    @Column(name="event_type", nullable=false, length=64) private String eventType;
    @Lob @Column(nullable=false) private String payload;
    @Column(name="correlation_id", nullable=false, length=64) private String correlationId;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16) private State state;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="published_at") private Instant publishedAt;
    @Column(name="retry_count", nullable=false) private int retryCount;
    @Column(name="last_error", length=1000) private String lastError;
    protected OutboxEvent() { }
    public OutboxEvent(String id,String aggregateId,String eventType,String payload,String correlationId,Instant createdAt){this.id=id;this.aggregateId=aggregateId;this.eventType=eventType;this.payload=payload;this.correlationId=correlationId;this.createdAt=createdAt;this.state=State.PENDING;}
    public void published(Instant at){state=State.PUBLISHED;publishedAt=at;lastError=null;}
    public void retry(String error){retryCount++;lastError=error==null?"unknown":error.substring(0,Math.min(1000,error.length()));}
    public String getId(){return id;} public String getAggregateId(){return aggregateId;} public String getEventType(){return eventType;} public String getPayload(){return payload;} public String getCorrelationId(){return correlationId;} public int getRetryCount(){return retryCount;}
}
