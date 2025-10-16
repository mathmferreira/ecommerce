package com.techmath.ecommerce.presentation.controllers;

import com.techmath.ecommerce.application.converters.OrderConverter;
import com.techmath.ecommerce.application.services.OrderService;
import com.techmath.ecommerce.presentation.dto.request.OrderItemsRequest;
import com.techmath.ecommerce.presentation.dto.response.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;
    private final OrderConverter converter;

    @PostMapping
    public OrderResponse createOrder(@RequestBody @Valid List<OrderItemsRequest> items) {
        var order = service.createOrder(items);
        return converter.toDTO(order);
    }

    @PostMapping("/pay/{orderId}")
    public void payOrder(@PathVariable Long orderId) {

    }

}
