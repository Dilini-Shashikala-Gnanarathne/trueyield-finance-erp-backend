package com.financeapp.user.exception;
public class PasswordResetRequiredException extends RuntimeException {
    public PasswordResetRequiredException(String message) { super(message); }
}
