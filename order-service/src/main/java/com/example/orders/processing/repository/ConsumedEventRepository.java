package com.example.orders.processing.repository;
import com.example.orders.processing.domain.ConsumedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ConsumedEventRepository extends JpaRepository<ConsumedEvent,String> { }
