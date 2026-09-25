package com.example.orders.acceptance.api;

import com.example.orders.acceptance.service.OrderAcceptanceService;
import com.example.orders.acceptance.service.OrderQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderAcceptanceService acceptance;
    private final OrderQueryService query;

    public OrderController(OrderAcceptanceService acceptance, OrderQueryService query) {
        this.acceptance = acceptance; this.query = query;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) { return acceptance.accept(request); }

    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable String orderId) { return query.find(orderId); }
}
