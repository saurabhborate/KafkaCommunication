package com.example.orders.acceptance.service;

import com.example.orders.acceptance.domain.Order;
import com.example.orders.acceptance.api.CreateOrderRequest;
import com.example.orders.acceptance.api.OrderResponse;
import com.example.orders.acceptance.outbox.OutboxWriter;
import com.example.orders.acceptance.repository.OrderRepository;
import com.example.orders.contracts.OrderAcceptedEvent;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class OrderAcceptanceService {
    private final OrderRepository orders;
    private final OutboxWriter outbox;
    private final ObjectMapper objectMapper;

    public OrderAcceptanceService(OrderRepository orders, OutboxWriter outbox, ObjectMapper objectMapper) {
        this.orders = orders;
        this.outbox = outbox;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderResponse accept(CreateOrderRequest request) {
        String orderId = "ORD-" + UUID.randomUUID();
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) correlationId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Order order = orders.save(new Order(orderId, request.customerId().trim(),
                request.product().trim(), request.quantity(), request.amount(), now));
        OrderAcceptedEvent event = new OrderAcceptedEvent(UUID.randomUUID().toString(),
                OrderAcceptedEvent.TYPE, OrderAcceptedEvent.VERSION, now, correlationId, orderId,
                order.getCustomerId(), order.getProduct(), order.getQuantity(), order.getAmount());
        try {
            outbox.append(orderId, event.eventType(), objectMapper.writeValueAsString(event), correlationId, now);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Could not serialize ORDER_ACCEPTED event", ex);
        }
        return OrderResponse.from(order);
    }

}
