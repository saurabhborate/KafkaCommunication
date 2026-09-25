package com.example.orders.acceptance.api;

import com.example.orders.acceptance.domain.Order;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(String orderId, String customerId, String product, int quantity,
                            BigDecimal amount, String status, Instant createdAt, Instant updatedAt) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(o.getOrderId(), o.getCustomerId(), o.getProduct(), o.getQuantity(),
                o.getAmount(), o.getStatus().name(), o.getCreatedAt(), o.getUpdatedAt());
    }
}
