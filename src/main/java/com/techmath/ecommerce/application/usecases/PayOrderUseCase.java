package com.techmath.ecommerce.application.usecases;

import com.techmath.ecommerce.application.converters.OrderConverter;
import com.techmath.ecommerce.application.services.OrderService;
import com.techmath.ecommerce.domain.events.OrderPaidEvent;
import com.techmath.ecommerce.infrastructure.messaging.producers.OrderEventProducer;
import com.techmath.ecommerce.presentation.dto.response.OrderResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayOrderUseCase {

    private final OrderService orderService;
    private final OrderConverter orderConverter;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderResponse execute(UUID orderId) {
        var order = orderService.getOrderById(orderId).orElseThrow(EntityNotFoundException::new);
        order.processPayment();
        order = orderService.updateOrder(order);
        var event = new OrderPaidEvent(order.getId(), order.getTotalAmount(), LocalDateTime.now());
        orderEventProducer.publishOrderPaidEvent(event);
        return orderConverter.toDTO(order);
    }

}
