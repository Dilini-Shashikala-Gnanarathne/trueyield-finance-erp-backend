package com.financeapp.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String eventId;
    private String eventType; // ORDER_CREATED, ORDER_ACCEPTED, ORDER_REJECTED, ORDER_PAID, ORDER_CANCELLED
    private String orderId;
    private String orderNumber;
    private String buyerId;
    private String farmerId;
    private String listingId;
    private String quantity;
    private String totalAmount;
    private String status;
    private String timestamp;
}
