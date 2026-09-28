package com.financeapp.order.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FarmerDashboardDto {
    private String farmerId;
    private long pendingOrders;
    private long completedOrders;
    private long rejectedOrders;
    private BigDecimal totalSales;
}
