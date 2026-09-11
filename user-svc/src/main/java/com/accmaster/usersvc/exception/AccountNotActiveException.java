package com.accmaster.usersvc.exception;
public class AccountNotActiveException extends RuntimeException {
    public AccountNotActiveException(String message) { super(message); }
}
