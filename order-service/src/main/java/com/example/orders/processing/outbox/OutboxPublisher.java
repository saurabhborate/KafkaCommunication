package com.example.orders.processing.outbox;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger; import org.slf4j.LoggerFactory; import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate; import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Component;
@Component
public class OutboxPublisher {
    private static final Logger log=LoggerFactory.getLogger(OutboxPublisher.class); private final OutboxEventRepository repo; private final KafkaTemplate<String,String> kafka;
    public OutboxPublisher(OutboxEventRepository repo,KafkaTemplate<String,String> kafka){this.repo=repo;this.kafka=kafka;}
    @Scheduled(fixedDelayString="${app.outbox.poll-ms:500}") public void publish(){repo.findTop50ByStateOrderByCreatedAtAsc(OutboxEvent.State.PENDING).forEach(this::publishOne);}
    private void publishOne(OutboxEvent e){MDC.put("correlationId",e.getCorrelationId());MDC.put("eventId",e.getId());MDC.put("orderId",e.getAggregateId());MDC.put("eventType",e.getEventType());try{kafka.send("orders.processed",e.getAggregateId(),e.getPayload()).get(10,TimeUnit.SECONDS);update(e.getId(),true,null);log.info("Outbox event published retryCount={}",e.getRetryCount());}catch(Exception ex){update(e.getId(),false,ex.toString());log.warn("Outbox publish failed and will retry",ex);}finally{MDC.clear();}}
    public void update(String id,boolean done,String error){if(done)repo.markPublished(id,Instant.now());else repo.markRetry(id,error.substring(0,Math.min(1000,error.length())));}
}
