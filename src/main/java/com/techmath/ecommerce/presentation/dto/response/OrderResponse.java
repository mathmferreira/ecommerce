package com.techmath.ecommerce.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techmath.ecommerce.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

    private UUID orderId;
    private OrderStatus status;
    private String message;

}
