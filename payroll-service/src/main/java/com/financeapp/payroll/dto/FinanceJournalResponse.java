package com.financeapp.payroll.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Internal DTO returned from {@code FinanceClient} to {@code PayrollService}.
 *
 * <p>Shields the PayrollService from knowing whether the response came
 * from gRPC or REST — the service always receives this neutral object.</p>
 */
@Data
@Builder
public class FinanceJournalResponse {

    private String journalReference;
    private String status;
    private String message;
    private boolean wasIdempotent;
}
