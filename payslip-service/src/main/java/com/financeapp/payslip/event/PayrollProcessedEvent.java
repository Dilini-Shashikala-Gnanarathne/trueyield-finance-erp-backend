package com.financeapp.payslip.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Consumer-side event DTO for {@code payroll.events} topic.
 * Mirrors the producer's {@code PayrollProcessedEvent} from payroll-service.
 *
 * <p>{@code @JsonIgnoreProperties(ignoreUnknown = true)} ensures forward-compatibility —
 * if payroll-service adds new fields in the future, payslip-service won't break.</p>
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayrollProcessedEvent {

    private String payrollReference;
    private String payrollPeriod;
    private int employeeCount;
    private BigDecimal totalAmount;
    private String currency;
    private String status;
    private String financeTransport;
    private String journalReference;
    private String createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant occurredAt;
}
