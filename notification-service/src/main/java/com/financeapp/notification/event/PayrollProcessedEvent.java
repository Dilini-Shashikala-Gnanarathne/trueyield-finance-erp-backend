package com.financeapp.notification.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Kafka event consumed from the {@code payroll.events} topic.
 *
 * <p>This class mirrors {@code PayrollProcessedEvent} in payroll-service.
 * In a real project, these would share a common library (e.g. a {@code events-contracts}
 * Maven module) to keep them in sync. For this demo they are duplicated for simplicity.</p>
 *
 * <p>{@code @JsonIgnoreProperties(ignoreUnknown = true)} ensures this consumer
 * can handle newer event versions that add fields without breaking.</p>
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
