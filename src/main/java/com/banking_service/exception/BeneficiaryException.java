package com.banking_service.exception;

public class BeneficiaryException extends RuntimeException {

    public BeneficiaryException(String message) {
        super(message);
    }

    public BeneficiaryException(String message, Throwable cause) {
        super(message, cause);
    }
}

