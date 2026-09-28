package com.financeapp.notification.listener;

import com.financeapp.notification.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {

    @KafkaListener(
            topics = "${kafka.topic.order-events:order.events}",
            groupId = "${spring.kafka.consumer.group-id:notification-service}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderEvent(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  KAFKA EVENT RECEIVED — order.events                     ║");
        log.info("╟──────────────────────────────────────────────────────────║");
        log.info("║  Order ID   : {}                                         ", event.getOrderId());
        log.info("║  Status     : {}                                         ", event.getStatus());
        log.info("╚══════════════════════════════════════════════════════════╝");

        try {
            switch (event.getStatus()) {
                case "PENDING":
                    log.info("📧 [EMAIL] Sending New Order Notification to Farmer ID: {}", event.getFarmerId());
                    log.info("📱 [SMS] Alerting Farmer {} about New Order {}", event.getFarmerId(), event.getOrderNumber());
                    break;
                case "ACCEPTED":
                    log.info("📧 [EMAIL] Notifying Buyer ID: {} that Order {} is ACCEPTED", event.getBuyerId(), event.getOrderNumber());
                    break;
                case "REJECTED":
                    log.info("📧 [EMAIL] Notifying Buyer ID: {} that Order {} was REJECTED", event.getBuyerId(), event.getOrderNumber());
                    break;
                case "FULFILLED":
                    log.info("📧 [EMAIL] Notifying Buyer ID: {} that Order {} is READY/FULFILLED for collection/delivery", event.getBuyerId(), event.getOrderNumber());
                    break;
                case "COMPLETED":
                    log.info("📧 [EMAIL] Notifying Farmer ID: {} and Buyer ID: {} that Order {} is COMPLETED", 
                             event.getFarmerId(), event.getBuyerId(), event.getOrderNumber());
                    break;
                default:
                    log.info("ℹ️ Unhandled order status transition for order {}: {}", event.getOrderNumber(), event.getStatus());
            }

            // Acknowledge offset manually
            ack.acknowledge();
            log.info("✅ OrderEvent processed successfully for order: {}", event.getOrderNumber());
        } catch (Exception e) {
            log.error("❌ Error processing OrderEvent for order: {}", event.getOrderNumber(), e);
        }
    }
}
