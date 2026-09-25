package com.example.orders.processing.service;

import com.example.orders.contracts.OrderAcceptedEvent;
import com.example.orders.contracts.OrderProcessedEvent;
import com.example.orders.processing.domain.ConsumedEvent;
import com.example.orders.processing.domain.OrderProcessing;
import com.example.orders.processing.outbox.OutboxEvent;
import com.example.orders.processing.outbox.OutboxEventRepository;
import com.example.orders.processing.repository.ConsumedEventRepository;
import com.example.orders.processing.repository.OrderProcessingRepository;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class OrderProcessingService {
    private static final Logger log= LoggerFactory.getLogger(OrderProcessingService.class);
    private final OrderProcessingRepository orders; private final ConsumedEventRepository consumed;
    private final OutboxEventRepository outbox; private final ObjectMapper mapper;
    public OrderProcessingService(OrderProcessingRepository orders, ConsumedEventRepository consumed, OutboxEventRepository outbox,ObjectMapper mapper){this.orders=orders;this.consumed=consumed;this.outbox=outbox;this.mapper=mapper;}
    @Transactional
    public void process(OrderAcceptedEvent event) {
        if (consumed.existsById(event.eventId()) || orders.existsById(event.orderId())) { log.info("Duplicate ORDER_ACCEPTED ignored"); return; }
        Instant now=Instant.now();
        OrderProcessing order=orders.save(new OrderProcessing(event.eventId(),event.orderId(),event.customerId(),event.product(),event.quantity(),event.amount(),now));
        order.processed(now);
        OrderProcessedEvent processed=new OrderProcessedEvent(UUID.randomUUID().toString(),OrderProcessedEvent.TYPE,OrderProcessedEvent.VERSION,now,event.correlationId(),order.getOrderId(),order.getCustomerId(),order.getProduct(),order.getQuantity(),order.getAmount(),OrderProcessedEvent.PROCESSED);
        try { outbox.save(new OutboxEvent(UUID.randomUUID().toString(),order.getOrderId(),processed.eventType(),mapper.writeValueAsString(processed),event.correlationId(),now)); }
        catch (JacksonException ex) { throw new IllegalStateException("Could not serialize ORDER_PROCESSED event",ex); }
        consumed.save(new ConsumedEvent(event.eventId(),event.eventType(),now));
        log.info("Order processed and ORDER_PROCESSED event placed in outbox");
    }
}
