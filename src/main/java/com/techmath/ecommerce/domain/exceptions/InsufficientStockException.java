package com.techmath.ecommerce.domain.exceptions;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(Integer stockQuantity) {
        super("Insufficient Stock. Available: " + stockQuantity);
    }

}
