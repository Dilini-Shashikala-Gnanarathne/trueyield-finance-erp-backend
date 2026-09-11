package com.financeapp.user.exception;
public class AccountNotActiveException extends RuntimeException {
    public AccountNotActiveException(String message) { super(message); }
}
