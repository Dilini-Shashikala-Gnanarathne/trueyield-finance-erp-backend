package com.financeapp.payroll.domain;

/**
 * Lifecycle states for a Payroll record.
 *
 * <p>Valid state transitions:
 * <pre>
 *   PENDING → PROCESSING → PROCESSED
 *                        → FAILED
 * </pre>
 * </p>
 */
public enum PayrollStatus {

    /** Payroll record created but Finance Service not yet called. */
    PENDING,

    /** Finance Service gRPC call in progress. */
    PROCESSING,

    /** Journal entry successfully created in Finance Service. */
    PROCESSED,

    /** Finance Service call failed; payroll could not be completed. */
    FAILED
}
