package com.techmath.ecommerce.infrastructure.messaging.consumers;

import com.techmath.ecommerce.application.services.OrderService;
import com.techmath.ecommerce.application.services.ProductService;
import com.techmath.ecommerce.domain.events.OrderPaidEvent;
import com.techmath.ecommerce.domain.exceptions.InsufficientStockException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidConsumer {

    private final OrderService orderService;
    private final ProductService productService;

    @KafkaListener(
            topics = "${kafka.topics.order-paid}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleOrderPaid(OrderPaidEvent event, Acknowledgment acknowledgment) {
        log.info("Received OrderPaidEvent for order: {}", event.getOrderId());
        var order = orderService.getOrderByIdWithItems(event.getOrderId()).orElseThrow(EntityNotFoundException::new);

        try {
            for (var item : order.getItems()) {
                productService.updateProductStock(item.getProduct(), item.getQuantity());
            }

            acknowledgment.acknowledge();
        } catch (InsufficientStockException e) {
            productService.handleInsufficientStock(order);
        }

        log.info("OrderPaidEvent processed successfully for order: {}", event.getOrderId());
    }

}