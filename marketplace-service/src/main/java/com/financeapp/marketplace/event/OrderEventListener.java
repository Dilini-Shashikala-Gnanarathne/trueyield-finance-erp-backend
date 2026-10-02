package com.financeapp.marketplace.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.marketplace.dto.listing.ReserveStockRequest;
import com.financeapp.marketplace.service.ListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final ListingService listingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.events", groupId = "marketplace-stock-group")
    public void handleOrderEvent(String message) {
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            log.info("Received order event: orderId={}, status={}", event.getOrderId(), event.getStatus());

            if ("ORDER_REJECTED".equals(event.getStatus()) || "ORDER_CANCELLED".equals(event.getStatus())) {
                ReserveStockRequest releaseReq = new ReserveStockRequest();
                releaseReq.setQuantity(event.getQuantity());
                listingService.releaseStock(event.getListingId(), releaseReq);
                log.info("Stock released successfully for cancelled/rejected order: {}", event.getOrderId());
            } else if ("ORDER_COMPLETED".equals(event.getStatus())) {
                // Could implement logic to permanently reduce totalQuantity here
            }

        } catch (Exception e) {
            log.error("Failed to process order event: {}", message, e);
        }
    }
}
