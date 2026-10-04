package com.financeapp.finance.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.finance.service.JournalEntryApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes order events and posts the settlement journal when an order is paid.
 *
 * <p>Failures are deliberately propagated (not swallowed) so the container's
 * {@code DefaultErrorHandler} can retry and finally route the record to the
 * {@code order.events.DLT} dead-letter topic. Posting is idempotent on the journal
 * reference, so redelivery is safe.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final JournalEntryApplicationService journalEntryApplicationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.events", groupId = "finance-journal-group")
    public void handleOrderEvent(String message) throws Exception {
        OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
        log.info("Received order event: orderId={}, status={}", event.getOrderId(), event.getStatus());

        // Cash is received when the buyer pays; post once on ORDER_PAID.
        // ORDER_COMPLETED needs no further posting (revenue split was booked at payment).
        if (!"ORDER_PAID".equals(event.getStatus())) {
            return;
        }

        journalEntryApplicationService.createOrderSettlementJournal(
                "ORD-PAY-" + event.getOrderId(),
                "Settlement for order " + event.getOrderNumber(),
                event.getTotalAmount(),
                event.getPlatformFee(),
                event.getDeliveryFee(),
                event.getCurrency() != null ? event.getCurrency() : "LKR",
                "order-service",
                event.getOrderId(),
                event.getFarmerId(),
                event.getBuyerId());
        log.info("Auto-posted settlement journal for paid order: {}", event.getOrderId());
    }
}
