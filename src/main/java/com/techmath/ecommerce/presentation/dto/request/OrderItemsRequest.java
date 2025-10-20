package com.techmath.ecommerce.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record OrderItemsRequest(
        @NotNull
        UUID productId,

        @NotNull
        @Positive
        Integer quantity
){}
