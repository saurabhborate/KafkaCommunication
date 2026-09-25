package com.example.orders.notifications.domain;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant;
@Entity @Table(name="notifications", uniqueConstraints=@UniqueConstraint(name="uk_notification_event",columnNames="event_id"))
public class Notification {
 @Id @Column(name="notification_id",length=64) private String notificationId;
 @Column(name="event_id",nullable=false,length=64) private String eventId;
 @Column(name="order_id",nullable=false,length=64) private String orderId;
 @Column(name="customer_id",nullable=false,length=128) private String customerId;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal amount;
 @Column(name="payment_status",nullable=false,length=32) private String paymentStatus;
 @Column(name="notification_status",nullable=false,length=32) private String notificationStatus;
 @Column(name="created_at",nullable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 protected Notification(){}
 public Notification(String id,String eventId,String orderId,String customerId,BigDecimal amount,String paymentStatus,Instant now){this.notificationId=id;this.eventId=eventId;this.orderId=orderId;this.customerId=customerId;this.amount=amount;this.paymentStatus=paymentStatus;this.notificationStatus="RECORDED";this.createdAt=now;this.updatedAt=now;}
 public String getNotificationId(){return notificationId;} public String getEventId(){return eventId;} public String getOrderId(){return orderId;} public String getCustomerId(){return customerId;} public BigDecimal getAmount(){return amount;} public String getPaymentStatus(){return paymentStatus;} public String getNotificationStatus(){return notificationStatus;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
