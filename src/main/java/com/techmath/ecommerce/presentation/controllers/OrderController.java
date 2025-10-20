package com.techmath.ecommerce.presentation.controllers;

import com.techmath.ecommerce.application.converters.OrderConverter;
import com.techmath.ecommerce.application.services.OrderService;
import com.techmath.ecommerce.application.usecases.PayOrderUseCase;
import com.techmath.ecommerce.presentation.dto.request.OrderItemsRequest;
import com.techmath.ecommerce.presentation.dto.response.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;
    private final OrderConverter converter;
    private final PayOrderUseCase payOrderUseCase;

    @PostMapping
    public OrderResponse createOrder(@RequestBody @Valid List<OrderItemsRequest> items) {
        var order = service.createOrder(items);
        return converter.toDTO(order);
    }

    @PostMapping("/pay/{orderId}")
    public OrderResponse  payOrder(@PathVariable UUID orderId) {
        return payOrderUseCase.execute(orderId);
    }

}
