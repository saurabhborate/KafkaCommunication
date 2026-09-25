package com.example.orders.processing.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="consumed_events")
public class ConsumedEvent {
    @Id @Column(name="event_id", length=64) private String eventId;
    @Column(name="event_type", nullable=false, length=64) private String eventType;
    @Column(name="consumed_at", nullable=false) private Instant consumedAt;
    protected ConsumedEvent() { }
    public ConsumedEvent(String id, String type, Instant at){eventId=id;eventType=type;consumedAt=at;}
}
