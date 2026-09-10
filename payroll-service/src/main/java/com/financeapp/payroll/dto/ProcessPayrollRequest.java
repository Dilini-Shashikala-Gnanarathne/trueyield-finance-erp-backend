package com.financeapp.payroll.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Inbound REST request DTO for the Process Payroll endpoint.
 *
 * <p>Using a dedicated DTO (rather than exposing the JPA entity directly) provides:
 * <ul>
 *   <li>Decoupling — the API contract is independent of the persistence model.</li>
 *   <li>Validation — Bean Validation annotations apply only to the API layer.</li>
 *   <li>Security — prevents over-posting attacks (mass assignment).</li>
 * </ul>
 * </p>
 */
@Data
public class ProcessPayrollRequest {

    /**
     * Payroll period in YYYY-MM format, e.g., "2026-08".
     * Must not be blank.
     */
    @NotBlank(message = "Payroll period is required")
    @Pattern(
            regexp = "^\\d{4}-(0[1-9]|1[0-2])$",
            message = "Payroll period must be in YYYY-MM format, e.g., 2026-08"
    )
    private String payrollPeriod;

    /**
     * Number of employees to include in this payroll run.
     * Must be at least 1 and at most 10,000 (demonstration limit).
     */
    @NotNull(message = "Employee count is required")
    @Min(value = 1, message = "Employee count must be at least 1")
    @Max(value = 10_000, message = "Employee count cannot exceed 10,000 for this demonstration")
    private Integer employeeCount;
}
