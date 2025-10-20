package com.techmath.ecommerce.application.services;

import com.techmath.ecommerce.domain.entities.Order;
import com.techmath.ecommerce.domain.entities.Product;
import com.techmath.ecommerce.domain.enums.OrderStatus;
import com.techmath.ecommerce.domain.exceptions.InsufficientStockException;
import com.techmath.ecommerce.domain.repositories.OrderRepository;
import com.techmath.ecommerce.presentation.dto.request.OrderItemsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService - Unit Tests")
class OrderServiceTests {

    @Mock
    private OrderRepository repository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private Product product;
    private UUID productId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        product = Product.builder()
                .id(productId)
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .category("Electronics")
                .stockQuantity(10)
                .build();
    }

    @Test
    @DisplayName("Should create order successfully with sufficient stock")
    void shouldCreateOrderSuccessfullyWithSufficientStock() {
        var itemRequest = new OrderItemsRequest(productId, 5);
        var items = List.of(itemRequest);

        when(productService.getProductById(productId)).thenReturn(product);
        when(repository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(orderId);
            return order;
        });

        var result = orderService.createOrder(items);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(499.95));
        verify(repository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should cancel order when stock is insufficient")
    void shouldCancelOrderWhenStockIsInsufficient() {
        var itemRequest = new OrderItemsRequest(productId, 15);
        var items = List.of(itemRequest);

        when(productService.getProductById(productId)).thenReturn(product);
        when(repository.saveAndFlush(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new InsufficientStockException("Insufficient stock"))
                .when(productService).handleInsufficientStock(any(Order.class));

        assertThatThrownBy(() -> orderService.createOrder(items))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");

        verify(repository, times(1)).saveAndFlush(any(Order.class));
        verify(productService, times(1)).handleInsufficientStock(any(Order.class));
    }

    @Test
    @DisplayName("Should get order by id successfully")
    void shouldGetOrderByIdSuccessfully() {
        var order = Order.builder()
                .id(orderId)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderById(orderId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(orderId);
        verify(repository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Should get order by id with items")
    void shouldGetOrderByIdWithItems() {
        var order = Order.builder()
                .id(orderId)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        when(repository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderByIdWithItems(orderId);

        assertThat(result).isPresent();
        verify(repository, times(1)).findByIdWithItems(orderId);
    }

    @Test
    @DisplayName("Should update order successfully")
    void shouldUpdateOrderSuccessfully() {
        var order = Order.builder()
                .id(orderId)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100))
                .build();

        when(repository.save(order)).thenReturn(order);

        var result = orderService.updateOrder(order);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        verify(repository, times(1)).save(order);
    }

}
