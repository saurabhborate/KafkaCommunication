package com.example.orders.acceptance.outbox;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafka;

    public OutboxPublisher(OutboxEventRepository repository, KafkaTemplate<String, String> kafka) {
        this.repository = repository;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-ms:500}")
    public void publishPending() {
        repository.findTop50ByStateOrderByCreatedAtAsc(OutboxEvent.State.PENDING)
                .forEach(this::publishOne);
    }

    private void publishOne(OutboxEvent event) {
        MDC.put("correlationId", event.getCorrelationId());
        MDC.put("eventId", event.getId());
        MDC.put("orderId", event.getAggregateId());
        MDC.put("eventType", event.getEventType());
        try {
            kafka.send(event.getEventType().equals("ORDER_ACCEPTED") ? "orders.accepted" : "orders.processed",
                    event.getAggregateId(), event.getPayload()).get(10, TimeUnit.SECONDS);
            markPublished(event.getId());
            log.info("Outbox event published retryCount={}", event.getRetryCount());
        } catch (Exception ex) {
            markRetry(event.getId(), ex.toString());
            log.warn("Outbox publish failed; event remains pending retryCount={}", event.getRetryCount() + 1, ex);
        } finally {
            MDC.clear();
        }
    }

    public void markPublished(String id) { repository.markPublished(id, Instant.now()); }

    public void markRetry(String id, String error) { repository.markRetry(id, error.substring(0, Math.min(1000, error.length()))); }
}
