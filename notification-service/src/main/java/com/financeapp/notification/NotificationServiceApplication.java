package com.financeapp.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Notification Service — Kafka Consumer Application.
 *
 * <p>This service consumes {@code PayrollProcessedEvent} messages from the
 * {@code payroll.events} Kafka topic and triggers downstream notifications.
 *
 * <p>It is intentionally stateless (no database) — notifications are logged
 * to demonstrate the decoupled event-driven pattern. In production, this service
 * would call an email provider (SendGrid, SES), an SMS gateway (Twilio), or
 * write to a dedicated audit database.</p>
 *
 * <h2>Architectural Role</h2>
 * <pre>
 *   Payroll Service  ──▶  payroll.events (Kafka)  ──▶  Notification Service
 *                                                  ──▶  (future: Audit Service)
 *                                                  ──▶  (future: BI Pipeline)
 * </pre>
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
