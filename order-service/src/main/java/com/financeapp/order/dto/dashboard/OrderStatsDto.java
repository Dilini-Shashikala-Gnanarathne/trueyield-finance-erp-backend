package com.financeapp.order.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderStatsDto {
    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private BigDecimal totalPlatformRevenue;
}
