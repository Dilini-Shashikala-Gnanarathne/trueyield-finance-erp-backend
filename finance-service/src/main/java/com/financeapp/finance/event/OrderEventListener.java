package com.financeapp.finance.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.finance.dto.CreateJournalEntryRequest;
import com.financeapp.finance.service.JournalEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final JournalEntryService journalEntryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.events", groupId = "finance-journal-group")
    public void handleOrderEvent(String message) {
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            log.info("Received order event: orderId={}, status={}", event.getOrderId(), event.getStatus());

            if ("ORDER_PAID".equals(event.getStatus()) || "ORDER_COMPLETED".equals(event.getStatus())) {
                // Auto-post settlement journal
                // DEBIT Cash / CREDIT Revenue
                
                CreateJournalEntryRequest journalRequest = CreateJournalEntryRequest.builder()
                        .reference("ORD-" + event.getOrderId())
                        .description("Settlement for order " + event.getOrderId())
                        .debitAccount("CASH")
                        .creditAccount("REVENUE")
                        .amount(event.getTotalAmount().toString())
                        .currency("LKR")
                        .sourceSystem("order-service")
                        .entryType(com.financeapp.finance.grpc.proto.JournalEntryType.GENERAL_LEDGER)
                        .build();
                        
                journalEntryService.createEntry(journalRequest);
                log.info("Auto-posted journal entry for paid/completed order: {}", event.getOrderId());
            }

        } catch (Exception e) {
            log.error("Failed to process order event in finance service: {}", message, e);
        }
    }
}
