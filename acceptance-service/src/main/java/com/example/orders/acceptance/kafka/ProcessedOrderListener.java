package com.example.orders.acceptance.kafka;

import com.example.orders.acceptance.service.ProcessedOrderEventService;
import com.example.orders.contracts.OrderProcessedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;
import tools.jackson.databind.ObjectMapper;

@Component
public class ProcessedOrderListener {
    private final ObjectMapper mapper;
    private final ProcessedOrderEventService service;
    public ProcessedOrderListener(ObjectMapper mapper, ProcessedOrderEventService service) { this.mapper = mapper; this.service = service; }
    @KafkaListener(topics = "${app.kafka.processed-topic}", groupId = "acceptance-service-group")
    public void receive(String payload) throws Exception {
        OrderProcessedEvent event = mapper.readValue(payload, OrderProcessedEvent.class);
        MDC.put("correlationId", event.correlationId()); MDC.put("eventId", event.eventId()); MDC.put("orderId", event.orderId()); MDC.put("eventType", event.eventType());
        try { if (!OrderProcessedEvent.TYPE.equals(event.eventType()) || event.eventVersion() < 1) throw new IllegalArgumentException("Unsupported order processed event"); service.apply(event); }
        finally { MDC.clear(); }
    }
}
