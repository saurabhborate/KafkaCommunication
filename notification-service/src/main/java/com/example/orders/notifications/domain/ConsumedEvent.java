package com.example.orders.notifications.domain;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="consumed_events") public class ConsumedEvent {
 @Id @Column(name="event_id",length=64) private String eventId; @Column(name="consumed_at",nullable=false) private Instant consumedAt;
 protected ConsumedEvent(){} public ConsumedEvent(String id,Instant at){eventId=id;consumedAt=at;}
}
