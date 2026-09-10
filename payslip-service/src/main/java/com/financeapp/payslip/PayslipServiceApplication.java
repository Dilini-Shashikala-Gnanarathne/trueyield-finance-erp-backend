package com.financeapp.payslip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Payslip Service — Kafka Consumer Application.
 *
 * <p>Consumes {@code PayrollProcessedEvent} from the {@code payroll.events} topic
 * and generates an individual pay slip for every employee in the payroll run.</p>
 *
 * <h2>Fan-Out Pattern Demonstrated</h2>
 * <pre>
 *   Payroll Service  ──▶  payroll.events (Kafka)
 *                                  │
 *             ┌────────────────────┼────────────────────┐
 *             ▼                   ▼                     ▼
 *   notification-service   payslip-service         (future)
 *   group-id: notification  group-id: payslip       any-service
 *   📧 Email alerts         📄 Per-employee          audit, BI...
 *   📋 Audit logs              pay slips
 * </pre>
 *
 * <p>Both services consume the SAME event independently.
 * Payroll Service does not know either consumer exists.
 * You can add or remove consumers with zero changes to the producer.</p>
 */
@SpringBootApplication
public class PayslipServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayslipServiceApplication.class, args);
    }
}
