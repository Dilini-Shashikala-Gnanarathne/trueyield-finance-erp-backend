package com.accmaster.usersvc.exception;
public class PasswordResetRequiredException extends RuntimeException {
    public PasswordResetRequiredException(String message) { super(message); }
}
