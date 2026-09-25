package com.example.orders.processing.kafka;
import com.example.orders.contracts.OrderAcceptedEvent;
import com.example.orders.processing.service.OrderProcessingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;
import tools.jackson.databind.ObjectMapper;
@Component
public class AcceptedOrderListener {
    private final ObjectMapper mapper; private final OrderProcessingService service;
    public AcceptedOrderListener(ObjectMapper mapper,OrderProcessingService service){this.mapper=mapper;this.service=service;}
    @KafkaListener(topics="${app.kafka.accepted-topic}",groupId="order-service-group")
    public void receive(String payload) throws Exception {
        OrderAcceptedEvent event=mapper.readValue(payload,OrderAcceptedEvent.class);
        MDC.put("correlationId", event.correlationId()); MDC.put("eventId", event.eventId()); MDC.put("orderId", event.orderId()); MDC.put("eventType", event.eventType());
        try { if(!OrderAcceptedEvent.TYPE.equals(event.eventType())||event.eventVersion()<1||event.quantity()<1||event.amount()==null||event.amount().signum()<=0) throw new IllegalArgumentException("Invalid or unsupported ORDER_ACCEPTED event"); service.process(event); }
        finally { MDC.clear(); }
    }
}
