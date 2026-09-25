package com.example.orders.acceptance.service;

import com.example.orders.acceptance.domain.Order;
import com.example.orders.acceptance.repository.ConsumedEvent;
import com.example.orders.acceptance.repository.ConsumedEventRepository;
import com.example.orders.acceptance.repository.OrderRepository;
import com.example.orders.contracts.OrderProcessedEvent;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessedOrderEventService {
    private static final Logger log = LoggerFactory.getLogger(ProcessedOrderEventService.class);
    private final OrderRepository orders;
    private final ConsumedEventRepository consumedEvents;

    public ProcessedOrderEventService(OrderRepository orders, ConsumedEventRepository consumedEvents) {
        this.orders = orders;
        this.consumedEvents = consumedEvents;
    }

    @Transactional
    public void apply(OrderProcessedEvent event) {
        if (consumedEvents.existsById(event.eventId())) {
            log.info("Duplicate ORDER_PROCESSED ignored");
            return;
        }
        Order order = orders.findById(event.orderId()).orElseThrow(() -> new IllegalArgumentException("Unknown orderId: " + event.orderId()));
        order.markProcessed(event.occurredAt());
        consumedEvents.save(new ConsumedEvent(event.eventId(), event.eventType(), Instant.now()));
        log.info("Order status synchronized to PROCESSED");
    }
}
