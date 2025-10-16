package com.techmath.ecommerce.infrastructure.messaging.producers;

import com.techmath.ecommerce.domain.events.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.order-paid}")
    private String orderPaidTopic;

    public void publishOrderPaidEvent(OrderPaidEvent event) {
        log.info("Publishing OrderPaidEvent for order: {}", event.getOrderId());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(orderPaidTopic, event.getOrderId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("OrderPaidEvent published successfully for order: {} [offset: {}]",
                        event.getOrderId(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish OrderPaidEvent for order: {}",
                        event.getOrderId(), ex);
            }
        });
    }
}
