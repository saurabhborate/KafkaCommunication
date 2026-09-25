package com.example.orders.acceptance.service;
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String id) { super("Order not found: " + id); }
}
