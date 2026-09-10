package com.financeapp.payslip.listener;

import com.financeapp.payslip.event.PayrollProcessedEvent;
import com.financeapp.payslip.model.PaySlip;
import com.financeapp.payslip.service.PayslipGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Kafka consumer for the {@code payroll.events} topic.
 *
 * <h2>Fan-Out Proof</h2>
 * <p>This listener uses consumer group {@code payslip-service} — completely separate
 * from {@code notification-service}'s group. Kafka maintains independent offset
 * pointers per group, so:</p>
 * <ul>
 *   <li>Both services receive every message</li>
 *   <li>A slow payslip-service does not affect notification-service consumption</li>
 *   <li>Restarting payslip-service replays from its last committed offset (not notif's)</li>
 * </ul>
 *
 * <h2>Processing Flow per Event</h2>
 * <ol>
 *   <li>Receive {@code PayrollProcessedEvent} from Kafka</li>
 *   <li>Delegate to {@link PayslipGeneratorService} to produce per-employee slips</li>
 *   <li>Log summary report with breakdown stats</li>
 *   <li>Manually acknowledge offset (AckMode.MANUAL)</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayslipEventListener {

    private final PayslipGeneratorService generatorService;

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
        log.info("║  PAYSLIP SERVICE — Kafka Event Received                  ║");
        log.info("╟──────────────────────────────────────────────────────────║");
        log.info("║  Topic     : payroll.events                              ║");
        log.info("║  Partition : {}  /  Offset : {}                          ", partition, offset);
        log.info("║  Key       : {}                           ", key);
        log.info("║  Employees : {}                                          ", event.getEmployeeCount());
        log.info("║  Period    : {}                                          ", event.getPayrollPeriod());
        log.info("╚══════════════════════════════════════════════════════════╝");

        try {
            long startMs = System.currentTimeMillis();

            // Generate all pay slips
            List<PaySlip> slips = generatorService.generatePaySlips(event);

            long elapsedMs = System.currentTimeMillis() - startMs;

            // Summary report
            printSummaryReport(event, slips, elapsedMs);

            // Commit offset only after all slips have been generated
            ack.acknowledge();

            log.info("✅ Payslip generation complete. count={} reference={} partition={} offset={}",
                    slips.size(), event.getPayrollReference(), partition, offset);

        } catch (Exception e) {
            log.error("❌ Payslip generation FAILED for reference={}. Offset NOT committed — will retry. error={}",
                    event.getPayrollReference(), e.getMessage(), e);
            // Do NOT ack — Spring Kafka will redeliver this message after backoff
        }
    }

    private void printSummaryReport(PayrollProcessedEvent event, List<PaySlip> slips, long elapsedMs) {
        if (slips.isEmpty()) return;

        PaySlip sample = slips.get(0); // All slips have same rates in simulation

        log.info("""
                
                ╔══════════════════════════════════════════════════════════════╗
                ║             PAYSLIP GENERATION SUMMARY REPORT               ║
                ╠══════════════════════════════════════════════════════════════╣
                ║  Payroll Reference  : {}
                ║  Period             : {}
                ║  Total Employees    : {}
                ║  Gross Per Employee : {} {}
                ║  Tax Per Employee   : {} {} (20%%)
                ║  PF Per Employee    : {} {} (8%%)
                ║  Net Per Employee   : {} {}
                ╠══════════════════════════════════════════════════════════════╣
                ║  Processing Time    : {} ms
                ║  Status             : ✅ ALL SLIPS GENERATED
                ╚══════════════════════════════════════════════════════════════╝
                """,
                event.getPayrollReference(),
                event.getPayrollPeriod(),
                slips.size(),
                sample.getCurrency(), sample.getGrossSalary(),
                sample.getCurrency(), sample.getTaxDeduction(),
                sample.getCurrency(), sample.getProvidentFundDeduction(),
                sample.getCurrency(), sample.getNetPay(),
                elapsedMs
        );
    }
}
