package com.example.orders.processing.outbox;
import java.util.List;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
public interface OutboxEventRepository extends JpaRepository<OutboxEvent,String> {
 List<OutboxEvent> findTop50ByStateOrderByCreatedAtAsc(OutboxEvent.State state);
 @Modifying @Transactional @Query("update OutboxEvent e set e.state = com.example.orders.processing.outbox.OutboxEvent$State.PUBLISHED, e.publishedAt = :at, e.lastError = null where e.id = :id") int markPublished(@Param("id") String id,@Param("at") Instant at);
 @Modifying @Transactional @Query("update OutboxEvent e set e.retryCount = e.retryCount + 1, e.lastError = :error where e.id = :id") int markRetry(@Param("id") String id,@Param("error") String error);
}
