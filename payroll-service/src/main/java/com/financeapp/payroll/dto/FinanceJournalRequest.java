package com.financeapp.payroll.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Internal DTO passed from {@code PayrollService} to {@code FinanceClient}.
 *
 * <p>This decouples the business layer from the transport layer.
 * Whether gRPC or REST is used, the PayrollService always works with
 * this neutral FinanceJournalRequest object.</p>
 */
@Data
@Builder
public class FinanceJournalRequest {

    /** Idempotency key — must match the payroll reference. */
    private String reference;

    private String description;
    private String debitAccount;
    private String creditAccount;
    private BigDecimal amount;
    private String currency;
    private String sourceSystem;
}
