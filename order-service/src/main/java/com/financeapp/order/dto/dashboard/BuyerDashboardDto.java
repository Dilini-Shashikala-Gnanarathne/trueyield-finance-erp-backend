package com.financeapp.order.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BuyerDashboardDto {
    private String buyerId;
    private long pendingOrders;
    private long fulfilledOrders;
    private long completedOrders;
    private BigDecimal totalSpent;
}
