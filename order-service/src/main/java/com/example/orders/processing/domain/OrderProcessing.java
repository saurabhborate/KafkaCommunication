package com.example.orders.processing.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name="order_processing")
public class OrderProcessing {
    @Id @Column(name="order_id", length=64) private String orderId;
    @Column(name="accepted_event_id", nullable=false, unique=true, length=64) private String acceptedEventId;
    @Column(name="customer_id", nullable=false, length=128) private String customerId;
    @Column(nullable=false, length=255) private String product;
    @Column(nullable=false) private int quantity;
    @Column(nullable=false, precision=19, scale=2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=24) private ProcessingStatus status;
    @Column(name="received_at", nullable=false) private Instant receivedAt;
    @Column(name="processed_at") private Instant processedAt;
    protected OrderProcessing() { }
    public OrderProcessing(String eventId, String orderId, String customerId, String product, int quantity, BigDecimal amount, Instant now) {
        this.acceptedEventId=eventId; this.orderId=orderId; this.customerId=customerId; this.product=product;
        this.quantity=quantity; this.amount=amount; this.status=ProcessingStatus.RECEIVED; this.receivedAt=now;
    }
    public void processed(Instant at) { this.status=ProcessingStatus.PROCESSED; this.processedAt=at; }
    public String getOrderId(){return orderId;} public String getCustomerId(){return customerId;} public String getProduct(){return product;}
    public int getQuantity(){return quantity;} public BigDecimal getAmount(){return amount;} public ProcessingStatus getStatus(){return status;}
}
