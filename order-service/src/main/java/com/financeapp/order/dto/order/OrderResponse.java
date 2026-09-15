package com.financeapp.order.dto.order;

import com.financeapp.order.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String id;
    private String orderNumber;
    private String buyerId;
    private String farmerId;
    private String listingId;
    private String produceName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private BigDecimal platformFee;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private String currency;
    private OrderStatus status;
    private String deliveryAddress;
    private String buyerNotes;
    private String rejectionReason;
    private PriceBreakdownDto priceBreakdown;
    private Instant createdAt;
    private Instant updatedAt;
}
