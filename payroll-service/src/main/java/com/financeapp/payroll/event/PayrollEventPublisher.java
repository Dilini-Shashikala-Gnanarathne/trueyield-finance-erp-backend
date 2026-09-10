package com.financeapp.payroll.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Publishes {@link PayrollProcessedEvent} to the Kafka topic {@code payroll.events}.
 *
 * <h2>KAFKA TEMPORARILY DISABLED</h2>
 * <p>The Kafka broker is not available in the current environment.
 * This class is a no-op stub that logs a warning instead of publishing events.
 * To re-enable Kafka:</p>
 * <ol>
 *   <li>Uncomment the {@code spring-kafka} dependency in {@code pom.xml}</li>
 *   <li>Uncomment the {@code spring.kafka} block in {@code application.yml}</li>
 *   <li>Restore the original implementation below (see commented-out code)</li>
 * </ol>
 *
 * <h2>Original Design</h2>
 * <ul>
 *   <li><strong>Fire-and-forget</strong> — publish is async. Kafka failure is logged
 *       as a warning but does NOT throw an exception. The payroll is already PROCESSED
 *       in the database; failing the HTTP response due to a Kafka hiccup would be wrong.
 *       Consumers can catch up when Kafka recovers (committed offsets).</li>
 *   <li><strong>Message key = payrollReference</strong> — guarantees all events for
 *       the same payroll land on the same partition, preserving per-payroll ordering.</li>
 *   <li><strong>Acks = all</strong> — configured in application.yml. The broker waits
 *       for all in-sync replicas to acknowledge before confirming. Prevents message loss.</li>
 * </ul>
 */
@Slf4j
@Service
public class PayrollEventPublisher {

    static final String TOPIC = "payroll.events";

    // TODO: Re-enable when Kafka broker is available.
     private final KafkaTemplate<String, PayrollProcessedEvent> kafkaTemplate;

    public PayrollEventPublisher(KafkaTemplate<String, PayrollProcessedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * NO-OP STUB — logs a warning and skips publishing.
     * Restore the original Kafka implementation when the broker is available.
     */
    public void publishPayrollProcessed(PayrollProcessedEvent event) {
        log.warn("[KAFKA DISABLED] Skipping event publish to topic={} key={}. " +
                "Re-enable Kafka in pom.xml and application.yml to activate.",
                TOPIC, event.getPayrollReference());

        // ── Original Kafka implementation (restore when broker is available) ──────────
         log.info("Publishing PayrollProcessedEvent to topic={} key={}", TOPIC, event.getPayrollReference());
         try {
             CompletableFuture<SendResult<String, PayrollProcessedEvent>> future =
                     kafkaTemplate.send(TOPIC, event.getPayrollReference(), event);
             future.whenComplete((result, ex) -> {
                 if (ex != null) {
                     log.warn("Failed to publish PayrollProcessedEvent. key={} cause={}",
                             event.getPayrollReference(), ex.getMessage());
                 } else {
                     log.info("PayrollProcessedEvent published successfully. key={} partition={} offset={}",
                             event.getPayrollReference(),
                             result.getRecordMetadata().partition(),
                             result.getRecordMetadata().offset());
                 }
             });
         } catch (Exception ex) {
             log.warn("Could not dispatch PayrollProcessedEvent to Kafka (broker may be offline). key={} error={}",
                     event.getPayrollReference(), ex.getMessage());
         }
        // ─────────────────────────────────────────────────────────────────────────────
    }

    /**
     * NO-OP STUB — convenience overload, builds and discards the event.
     * Restore the original Kafka implementation when the broker is available.
     */
    public void publishPayrollProcessed(
            String payrollReference,
            String payrollPeriod,
            int employeeCount,
            BigDecimal totalAmount,
            String financeTransport,
            String journalReference,
            String createdAt) {

        PayrollProcessedEvent event = PayrollProcessedEvent.builder()
                .payrollReference(payrollReference)
                .payrollPeriod(payrollPeriod)
                .employeeCount(employeeCount)
                .totalAmount(totalAmount)
                .currency("USD")
                .status("PROCESSED")
                .financeTransport(financeTransport)
                .journalReference(journalReference)
                .createdAt(createdAt)
                .occurredAt(java.time.Instant.now())
                .build();

        publishPayrollProcessed(event);
    }
}
