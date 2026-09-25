package com.example.orders.acceptance.repository;

import com.example.orders.acceptance.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> { }
