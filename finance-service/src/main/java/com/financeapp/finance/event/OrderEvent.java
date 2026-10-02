package com.financeapp.finance.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String orderId;
    private String orderNumber;
    private String buyerId;
    private String farmerId;
    private String listingId;
    private BigDecimal quantity;
    private BigDecimal totalAmount;
    private String status;
    private String timestamp;
}
