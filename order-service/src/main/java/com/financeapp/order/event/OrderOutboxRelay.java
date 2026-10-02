package com.financeapp.order.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.order.domain.entity.OrderOutboxEntity;
import com.financeapp.order.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderOutboxRelay {

    private final OrderOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void relayEvents() {
        List<OrderOutboxEntity> pendingEvents = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        
        for (OrderOutboxEntity outbox : pendingEvents) {
            try {
                // Publish to Kafka
                kafkaTemplate.send("order.events", outbox.getAggregateId(), outbox.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.debug("Published outbox event id: {}", outbox.getId());
                        } else {
                            log.error("Failed to publish outbox event id: {}", outbox.getId(), ex);
                        }
                    });
                
                // Mark as processed immediately in the DB transaction
                outbox.setStatus("PROCESSED");
                outbox.setProcessedAt(Instant.now());
                outboxRepository.save(outbox);
                
            } catch (Exception e) {
                log.error("Error processing outbox event id: {}", outbox.getId(), e);
                // We don't throw here to allow other events to be processed
            }
        }
    }
}
