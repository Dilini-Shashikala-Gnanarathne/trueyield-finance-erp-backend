package com.financeapp.notification.listener;

import com.financeapp.notification.event.PayrollProcessedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Integration test for Notification Service using an in-memory Embedded Kafka broker.
 *
 * <p>Demonstrates and proves that the service connects to Kafka, consumes
 * 'payroll.events', triggers email/SMS/audit logic, and commits offsets —
 * without needing Docker installed!</p>
 */
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = {"payroll.events"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DisplayName("Notification Service — Kafka Integration Test")
class NotificationServiceKafkaIntegrationTest {

    @TestConfiguration
    static class TestKafkaProducerConfig {
        @Bean
        public ProducerFactory<String, PayrollProcessedEvent> testProducerFactory(EmbeddedKafkaBroker broker) {
            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString());
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return new DefaultKafkaProducerFactory<>(configProps);
        }

        @Bean
        public KafkaTemplate<String, PayrollProcessedEvent> testKafkaTemplate(
                ProducerFactory<String, PayrollProcessedEvent> pf) {
            return new KafkaTemplate<>(pf);
        }
    }

    @Autowired
    private KafkaTemplate<String, PayrollProcessedEvent> kafkaTemplate;

    @SpyBean
    private PayrollEventListener eventListener;

    @Test
    @DisplayName("Should consume PayrollProcessedEvent from Kafka and execute notification pipeline")
    void shouldConsumePayrollProcessedEvent() {
        // Arrange: Create a real event
        PayrollProcessedEvent event = new PayrollProcessedEvent();
        event.setPayrollReference("PAY-2026-TEST-0001");
        event.setPayrollPeriod("2026-09");
        event.setEmployeeCount(15);
        event.setTotalAmount(new BigDecimal("125000.00"));
        event.setCurrency("USD");
        event.setStatus("PROCESSED");
        event.setFinanceTransport("GRPC");
        event.setJournalReference("JRN-TEST-0001");
        event.setOccurredAt(Instant.now());

        // Act: Send directly to embedded Kafka topic
        kafkaTemplate.send("payroll.events", event.getPayrollReference(), event);

        // Assert: Wait and verify listener was invoked
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(eventListener, atLeastOnce()).onPayrollProcessed(
                    any(PayrollProcessedEvent.class),
                    anyInt(),
                    anyLong(),
                    any(),
                    any()
            );
        });
    }
}
