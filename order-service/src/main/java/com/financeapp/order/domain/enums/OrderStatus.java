package com.financeapp.order.domain.enums;

public enum OrderStatus {
    PENDING,
    ACCEPTED,
    PAID,
    REJECTED,
    FULFILLED,
    COMPLETED,
    CANCELLED;

    public boolean isTerminal() {
        return this == REJECTED || this == COMPLETED || this == CANCELLED;
    }
}
