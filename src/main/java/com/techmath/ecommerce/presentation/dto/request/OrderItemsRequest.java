package com.techmath.ecommerce.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemsRequest(
        @NotBlank
        UUID productId,

        @NotNull
        Integer quantity
){}
