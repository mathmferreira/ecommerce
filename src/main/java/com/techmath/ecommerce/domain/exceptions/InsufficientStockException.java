package com.techmath.ecommerce.domain.exceptions;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String message) {
        super(message);
    }

}
