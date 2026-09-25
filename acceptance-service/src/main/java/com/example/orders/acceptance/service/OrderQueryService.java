package com.example.orders.acceptance.service;

import com.example.orders.acceptance.api.OrderResponse;
import com.example.orders.acceptance.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderQueryService {
    private final OrderRepository orders;
    public OrderQueryService(OrderRepository orders) { this.orders = orders; }
    @Transactional(readOnly = true)
    public OrderResponse find(String id) { return orders.findById(id).map(OrderResponse::from).orElseThrow(() -> new OrderNotFoundException(id)); }
}
