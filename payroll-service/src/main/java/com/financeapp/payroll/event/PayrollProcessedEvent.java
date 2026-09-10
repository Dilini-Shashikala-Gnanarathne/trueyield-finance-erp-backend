package com.financeapp.payroll.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable event published to Kafka after a payroll run is successfully processed.
 *
 * <p>This event is the contract between the Payroll Service (producer) and
 * any downstream consumers (notification-service, audit-service, BI pipeline, etc.).
 * Consumers react to this event independently — Payroll Service does not know
 * which services are listening.</p>
 *
 * <h2>Topic</h2>
 * <pre>payroll.events</pre>
 *
 * <h2>Message Key</h2>
 * <p>{@code payrollReference} — ensures all events for the same payroll run
 * land on the same Kafka partition, preserving order per payroll.</p>
 *
 * <h2>Serialization</h2>
 * <p>JSON via Jackson. Amount is a BigDecimal string to preserve decimal precision —
 * same reasoning as Protobuf amount-as-string in the gRPC contract.</p>
 */
@Value
@Builder
@Jacksonized
public class PayrollProcessedEvent {

    /** Unique business reference, e.g. "PAY-2026-08-0001". Also the Kafka message key. */
    String payrollReference;

    /** Payroll period in YYYY-MM format, e.g. "2026-08". */
    String payrollPeriod;

    /** Number of employees included in this payroll run. */
    int employeeCount;

    /**
     * Total gross payroll amount.
     * BigDecimal preserves exact decimal precision — never use double for money.
     */
    BigDecimal totalAmount;

    /** ISO 4217 currency code. */
    String currency;

    /** Final status of the payroll record — always PROCESSED for this event. */
    String status;

    /**
     * Transport used to reach Finance Service: "GRPC" or "REST".
     * Useful for downstream analytics comparing transport performance.
     */
    String financeTransport;

    /** Reference to the journal entry created in Finance Service. */
    String journalReference;

    /** Timestamp when the payroll was created in Payroll Service. */
    String createdAt;

    /**
     * Timestamp when this event was published.
     * ISO-8601 instant — consumers use this for lag monitoring.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant occurredAt;
}
