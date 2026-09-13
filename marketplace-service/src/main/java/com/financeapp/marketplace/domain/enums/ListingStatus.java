package com.financeapp.marketplace.domain.enums;

import java.util.Set;

/**
 * Lifecycle states for agricultural produce listings (Section 9 of specification).
 * Recommended states: DRAFT -> ACTIVE -> SOLD_OUT / CANCELLED / EXPIRED.
 */
public enum ListingStatus {
    DRAFT,
    ACTIVE,
    SOLD_OUT,
    CANCELLED,
    EXPIRED;

    public boolean canTransitionTo(ListingStatus target) {
        if (this == target) {
            return true;
        }
        return switch (this) {
            case DRAFT -> target == ACTIVE || target == CANCELLED;
            case ACTIVE -> target == SOLD_OUT || target == CANCELLED || target == EXPIRED;
            case SOLD_OUT, CANCELLED, EXPIRED -> false; // Terminal states
        };
    }

    public boolean isTerminal() {
        return this == SOLD_OUT || this == CANCELLED || this == EXPIRED;
    }
}
