package com.financeapp.marketplace.domain.enums;

/**
 * Seller location privacy control (Section 5.2 of specification).
 * Protects farmer's exact location until an order is placed.
 */
public enum LocationVisibility {
    APPROXIMATE,
    EXACT_AFTER_ORDER
}
