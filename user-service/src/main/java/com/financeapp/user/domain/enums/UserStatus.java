package com.financeapp.user.domain.enums;

/**
 * Lifecycle states for a user account.
 * Matches usr_user.status column constraints.
 */
public enum UserStatus {
    NOT_VERIFIED,
    ACTIVE,
    DEACTIVE,
    SUSPENDED,
    DELETED
}
