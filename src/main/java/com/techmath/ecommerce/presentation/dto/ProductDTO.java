package com.techmath.ecommerce.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        UUID id,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than zero")
        BigDecimal price,

        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Stock quantity is required")
        @PositiveOrZero(message = "Stock quantity must be zero or positive")
        Integer stockQuantity,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        LocalDateTime createdAt,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        LocalDateTime updatedAt
) {}
