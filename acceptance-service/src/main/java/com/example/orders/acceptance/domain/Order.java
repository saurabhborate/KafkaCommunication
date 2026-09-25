package com.example.orders.acceptance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column(name = "order_id", length = 64, nullable = false)
    private String orderId;

    @Column(name = "customer_id", length = 128, nullable = false)
    private String customerId;

    @Column(nullable = false, length = 255)
    private String product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Order() { }

    public Order(String orderId, String customerId, String product, int quantity,
                 BigDecimal amount, Instant now) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.product = product;
        this.quantity = quantity;
        this.amount = amount;
        this.status = OrderStatus.ACCEPTED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void markProcessed(Instant now) {
        if (status != OrderStatus.PROCESSED) {
            this.status = OrderStatus.PROCESSED;
            this.updatedAt = now;
        }
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public BigDecimal getAmount() { return amount; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
