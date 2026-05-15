package com.weverse.controller;

import com.weverse.dto.*;
import com.weverse.service.OrderService;
import com.weverse.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final StockService stockService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse createOrder(@RequestBody @Valid CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{orderId}")
    public OrderDetailResponse getOrder(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    @GetMapping("/stock/{productId}")
    public StockResponse getStock(@PathVariable String productId) {
        return stockService.getStock(productId);
    }

    @PostMapping("/{orderId}/claim")
    public CancelOrderResponse cancelOrder(@PathVariable Long orderId,
                                           @RequestBody @Valid CancelOrderRequest request) {
        return orderService.cancelOrder(orderId, request);
    }
}
