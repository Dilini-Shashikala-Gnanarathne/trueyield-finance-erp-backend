package com.financeapp.user.domain.enums;

/**
 * Represents the high-level type of user account.
 * Used to drive strategy selection (LoginStrategyFactory).
 */
public enum UserType {
    ADMIN,
    STUDENT
}
