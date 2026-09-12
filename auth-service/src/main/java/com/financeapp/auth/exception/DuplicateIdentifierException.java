package com.financeapp.auth.exception;

public class DuplicateIdentifierException extends RuntimeException {
    public DuplicateIdentifierException(String message) {
        super(message);
    }
}
