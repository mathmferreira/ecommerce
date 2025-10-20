package com.techmath.ecommerce.config;

import com.techmath.ecommerce.infrastructure.messaging.producers.OrderEventProducer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class KafkaTestConfig {

    @Bean
    @Primary
    public OrderEventProducer orderEventProducer() {
        return mock(OrderEventProducer.class);
    }

}
