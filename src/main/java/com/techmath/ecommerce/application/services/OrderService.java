package com.techmath.ecommerce.application.services;

import com.techmath.ecommerce.domain.entities.Order;
import com.techmath.ecommerce.domain.exceptions.InsufficientStockException;
import com.techmath.ecommerce.domain.repositories.OrderRepository;
import com.techmath.ecommerce.presentation.dto.request.OrderItemsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final ProductService productService;

    @Transactional
    public Order createOrder(List<OrderItemsRequest> items) {
        var order = new Order();

        for (var item : items) {
            var product = productService.getProductById(item.productId());
            order.addItem(product, item.quantity());
            if (order.isPending() && !product.hasStock(item.quantity())) {
                order.cancel();
            }
        }

        if (order.isCancelled()) {
            repository.saveAndFlush(order);
            productService.handleInsufficientStock(order);
        }

        return repository.save(order);
    }

    public Optional<Order> getOrderById(UUID id) {
        return repository.findById(id);
    }

    public Optional<Order> getOrderByIdWithItems(UUID id) {
        return repository.findByIdWithItems(id);
    }

    public Order updateOrder(Order order) {
        return repository.save(order);
    }

}
