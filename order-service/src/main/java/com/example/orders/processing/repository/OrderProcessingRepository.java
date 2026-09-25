package com.example.orders.processing.repository;
import com.example.orders.processing.domain.OrderProcessing;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderProcessingRepository extends JpaRepository<OrderProcessing,String> { }
