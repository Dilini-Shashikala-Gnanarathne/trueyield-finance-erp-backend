package com.financeapp.payroll.exception;

/**
 * Thrown when a payroll for the given period has already been processed.
 * Maps to HTTP 409 Conflict.
 */
public class DuplicatePayrollException extends RuntimeException {

    private final String payrollPeriod;

    public DuplicatePayrollException(String payrollPeriod) {
        super("Payroll has already been processed for period: " + payrollPeriod);
        this.payrollPeriod = payrollPeriod;
    }

    public String getPayrollPeriod() {
        return payrollPeriod;
    }
}
