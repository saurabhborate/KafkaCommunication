package com.example.orders.notifications.repository;
import com.example.orders.notifications.domain.ConsumedEvent; import org.springframework.data.jpa.repository.JpaRepository;
public interface ConsumedEventRepository extends JpaRepository<ConsumedEvent,String>{}
