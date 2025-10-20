package com.techmath.ecommerce.application.usecases;

import com.techmath.ecommerce.application.converters.OrderConverter;
import com.techmath.ecommerce.application.services.OrderService;
import com.techmath.ecommerce.domain.entities.Order;
import com.techmath.ecommerce.domain.enums.OrderStatus;
import com.techmath.ecommerce.domain.events.OrderPaidEvent;
import com.techmath.ecommerce.infrastructure.messaging.producers.OrderEventProducer;
import com.techmath.ecommerce.presentation.dto.response.OrderResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayOrderUseCase - Unit Tests")
class PayOrderUseCaseTests {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderConverter orderConverter;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private PayOrderUseCase payOrderUseCase;

    private UUID orderId;
    private Order order;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();

        order = Order.builder()
                .id(orderId)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(999.99))
                .build();

        orderResponse = OrderResponse.builder()
                .orderId(orderId)
                .status(OrderStatus.PAID)
                .message("Order paid successfully")
                .build();
    }

    @Test
    @DisplayName("Should pay order successfully")
    void shouldPayOrderSuccessfully() {
        when(orderService.getOrderById(orderId)).thenReturn(Optional.of(order));
        when(orderService.updateOrder(any(Order.class))).thenReturn(order);
        when(orderConverter.toDTO(any(Order.class))).thenReturn(orderResponse);
        doNothing().when(orderEventProducer).publishOrderPaidEvent(any(OrderPaidEvent.class));

        var result = payOrderUseCase.execute(orderId);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAID);

        verify(orderService, times(1)).getOrderById(orderId);
        verify(orderService, times(1)).updateOrder(order);
        verify(orderConverter, times(1)).toDTO(order);
        verify(orderEventProducer, times(1)).publishOrderPaidEvent(any(OrderPaidEvent.class));
    }

    @Test
    @DisplayName("Should publish order paid event with correct data")
    void shouldPublishOrderPaidEventWithCorrectData() {
        when(orderService.getOrderById(orderId)).thenReturn(Optional.of(order));
        when(orderService.updateOrder(any(Order.class))).thenReturn(order);
        when(orderConverter.toDTO(any(Order.class))).thenReturn(orderResponse);

        var eventCaptor = ArgumentCaptor.forClass(OrderPaidEvent.class);

        payOrderUseCase.execute(orderId);

        verify(orderEventProducer).publishOrderPaidEvent(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(capturedEvent.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(999.99));
        assertThat(capturedEvent.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when order not found")
    void shouldThrowEntityNotFoundExceptionWhenOrderNotFound() {
        when(orderService.getOrderById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payOrderUseCase.execute(orderId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(orderService, times(1)).getOrderById(orderId);
        verify(orderService, never()).updateOrder(any(Order.class));
        verify(orderEventProducer, never()).publishOrderPaidEvent(any(OrderPaidEvent.class));
    }

    @Test
    @DisplayName("Should change order status to PAID")
    void shouldChangeOrderStatusToPaid() {
        when(orderService.getOrderById(orderId)).thenReturn(Optional.of(order));
        when(orderService.updateOrder(any(Order.class))).thenAnswer(invocation -> {
            Order orderArg = invocation.getArgument(0);
            assertThat(orderArg.getStatus()).isEqualTo(OrderStatus.PAID);
            return orderArg;
        });
        when(orderConverter.toDTO(any(Order.class))).thenReturn(orderResponse);

        payOrderUseCase.execute(orderId);

        verify(orderService).updateOrder(argThat(o -> o.getStatus() == OrderStatus.PAID));
    }

}
