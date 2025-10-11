package com.techmath.ecommerce.domain.exceptions;

public class InvalidOrderStateException extends BusinessException {

    public InvalidOrderStateException() {
        super("Orders can only be modified in pending status.");
    }

}
