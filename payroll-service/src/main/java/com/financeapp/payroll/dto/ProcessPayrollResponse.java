package com.financeapp.payroll.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Outbound REST response DTO for the Process Payroll endpoint.
 *
 * <p>This is the contract seen by API consumers (Angular frontend, integration partners, etc.).
 * It deliberately excludes internal fields such as database IDs and transport details.</p>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessPayrollResponse {

    /** Unique business reference for this payroll run, e.g., "PAY-2026-08-0001". */
    private String payrollReference;

    /** The period this payroll covers, e.g., "2026-08". */
    private String payrollPeriod;

    /** Number of employees included in this payroll run. */
    private Integer employeeCount;

    /**
     * Total gross payroll amount.
     * BigDecimal preserves exact decimal precision in JSON serialization.
     */
    private BigDecimal totalAmount;

    /** Current status of the payroll record. */
    private String status;

    /**
     * Reference to the journal entry created in Finance Service.
     * Null if Finance Service call failed or is pending.
     */
    private String journalReference;

    /** Transport used for Finance communication: "GRPC" or "REST". */
    private String financeTransport;

    /** Timestamp when the payroll was created. */
    private LocalDateTime createdAt;

    /** Human-readable message, used for error cases or additional context. */
    private String message;
}
