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
public class OrderSummaryResponse {

    private String id;
    private String orderNumber;
    private String listingId;
    private String produceName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal totalAmount;
    private String currency;
    private OrderStatus status;
    private Instant createdAt;
}
