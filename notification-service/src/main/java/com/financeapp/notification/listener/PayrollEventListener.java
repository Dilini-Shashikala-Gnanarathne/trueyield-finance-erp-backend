package com.financeapp.notification.listener;

import com.financeapp.notification.event.PayrollProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Kafka consumer for the {@code payroll.events} topic.
 *
 * <h2>Consumer Configuration</h2>
 * <ul>
 *   <li><strong>group-id</strong>: {@code notification-service} — independent consumer group.
 *       Adding another consumer group (e.g., audit-service) doesn't affect this one.</li>
 *   <li><strong>AckMode.MANUAL</strong>: Offset is committed only after all notification
 *       actions complete. If processing fails, the message is reprocessed on restart.</li>
 *   <li><strong>concurrency = 3</strong>: One listener thread per partition.
 *       Matches {@code KAFKA_NUM_PARTITIONS: 3} in docker-compose.</li>
 * </ul>
 *
 * <h2>What This Simulates</h2>
 * <p>In production, this listener would call:</p>
 * <ul>
 *   <li>📧 Email service (SendGrid / AWS SES) — payroll team confirmation</li>
 *   <li>📋 Audit service — compliance audit trail</li>
 *   <li>📊 BI/Analytics — trigger report refresh</li>
 *   <li>📱 SMS gateway (Twilio) — C-level alerts for large payrolls</li>
 * </ul>
 */
@Slf4j
@Component
public class PayrollEventListener {

    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                    .withZone(ZoneId.of("UTC"));

    private static final long LARGE_PAYROLL_THRESHOLD_USD = 1_000_000L;

    /**
     * Main listener method. Receives a {@link PayrollProcessedEvent}, performs
     * all notification actions, then manually acknowledges the offset.
     *
     * @param event    deserialized event payload
     * @param partition Kafka partition (for structured logging)
     * @param offset    Kafka offset (for structured logging and debugging)
     * @param key       Kafka message key = payrollReference
     * @param ack       manual acknowledgment handle
     */
    @KafkaListener(
            topics = "${kafka.topic.payroll-events:payroll.events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPayrollProcessed(
            @Payload PayrollProcessedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  KAFKA EVENT RECEIVED — payroll.events                   ║");
        log.info("╟──────────────────────────────────────────────────────────║");
        log.info("║  Partition  : {}                                          ", partition);
        log.info("║  Offset     : {}                                          ", offset);
        log.info("║  Key        : {}                             ", key);
        log.info("╚══════════════════════════════════════════════════════════╝");

        try {
            logEventDetails(event);
            simulateEmailNotification(event);
            simulateAuditLog(event);
            simulateBiPipelineTrigger(event);

            if (isLargePayroll(event)) {
                simulateLargePayrollAlert(event);
            }

            // Manually commit offset ONLY after all actions complete successfully.
            // If any action threw an exception, the offset is NOT committed and
            // Spring Kafka will retry the message according to retry configuration.
            ack.acknowledge();

            log.info("✅ PayrollProcessedEvent processed and offset committed. " +
                            "reference={} partition={} offset={}",
                    event.getPayrollReference(), partition, offset);

        } catch (Exception e) {
            log.error("❌ Error processing PayrollProcessedEvent. " +
                            "reference={} error={} — offset NOT committed, will retry.",
                    event.getPayrollReference(), e.getMessage(), e);
            // Do NOT call ack.acknowledge() — Kafka will redeliver this message
            // after error.backoff.delay.ms (configured in application.yml)
        }
    }

    // ── Simulated downstream actions ──────────────────────────────────────────

    private void logEventDetails(PayrollProcessedEvent event) {
        String occurredAt = event.getOccurredAt() != null
                ? TIMESTAMP_FMT.format(event.getOccurredAt())
                : "N/A";

        log.info("📋 Event Details:" +
                        "\n    Reference    : {}" +
                        "\n    Period       : {}" +
                        "\n    Employees    : {}" +
                        "\n    Total Amount : {} {}" +
                        "\n    Transport    : {}" +
                        "\n    Journal Ref  : {}" +
                        "\n    Occurred At  : {}",
                event.getPayrollReference(),
                event.getPayrollPeriod(),
                event.getEmployeeCount(),
                event.getTotalAmount(), event.getCurrency(),
                event.getFinanceTransport(),
                event.getJournalReference(),
                occurredAt);
    }

    /**
     * Simulates sending a confirmation email to the payroll department.
     * In production: call SendGrid / AWS SES API.
     */
    private void simulateEmailNotification(PayrollProcessedEvent event) {
        log.info("📧 [EMAIL] Sending payroll confirmation to payroll-team@company.com" +
                        "\n    Subject: Payroll {} processed — {} employees, {} {}",
                event.getPayrollReference(),
                event.getEmployeeCount(),
                event.getTotalAmount(),
                event.getCurrency());
        // In production: emailService.send(payrollTeamEmail, subject, body);
    }

    /**
     * Simulates writing an audit trail entry for compliance.
     * In production: write to an immutable audit database or AWS CloudTrail.
     */
    private void simulateAuditLog(PayrollProcessedEvent event) {
        log.info("📋 [AUDIT] Audit trail entry created." +
                        "\n    action    : PAYROLL_PROCESSED" +
                        "\n    reference : {}" +
                        "\n    amount    : {} {}" +
                        "\n    transport : {}" +
                        "\n    timestamp : {}",
                event.getPayrollReference(),
                event.getTotalAmount(),
                event.getCurrency(),
                event.getFinanceTransport(),
                Instant.now());
        // In production: auditRepository.save(new AuditEntry(...));
    }

    /**
     * Simulates triggering a BI/reporting pipeline refresh.
     * In production: call a Databricks job, Redshift COPY, or Airflow DAG trigger.
     */
    private void simulateBiPipelineTrigger(PayrollProcessedEvent event) {
        log.info("📊 [BI] Triggering payroll analytics pipeline refresh. period={}",
                event.getPayrollPeriod());
        // In production: biPipelineClient.trigger("payroll-summary", event.getPayrollPeriod());
    }

    /**
     * Simulates a high-priority SMS/Slack alert for unusually large payroll runs.
     * Threshold: > $1,000,000 USD.
     */
    private void simulateLargePayrollAlert(PayrollProcessedEvent event) {
        log.warn("🚨 [ALERT] LARGE PAYROLL DETECTED — CFO notification triggered!" +
                        "\n    Reference : {}" +
                        "\n    Amount    : {} {} ({} employees)" +
                        "\n    Threshold : > ${}",
                event.getPayrollReference(),
                event.getTotalAmount(),
                event.getCurrency(),
                event.getEmployeeCount(),
                LARGE_PAYROLL_THRESHOLD_USD);
        // In production: smsGateway.send(cfoPhone, alertMessage);
        // In production: slackClient.postMessage("#finance-alerts", alertMessage);
    }

    private boolean isLargePayroll(PayrollProcessedEvent event) {
        return event.getTotalAmount() != null &&
                event.getTotalAmount().longValue() > LARGE_PAYROLL_THRESHOLD_USD;
    }
}
