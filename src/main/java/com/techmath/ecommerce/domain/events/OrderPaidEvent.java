package com.techmath.ecommerce.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor @AllArgsConstructor
public class OrderPaidEvent {

    private UUID orderId;
    private BigDecimal totalAmount;
    private LocalDateTime paidAt;

}
