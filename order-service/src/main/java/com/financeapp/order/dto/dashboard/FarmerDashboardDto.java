package com.financeapp.order.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerDashboardDto {
    private String farmerId;
    private long pendingOrders;
    private long acceptedOrders;
    private long paidOrders;
    private long fulfilledOrders;
    private long completedOrders;
    private long rejectedOrders;
    private long cancelledOrders;
    private BigDecimal totalSales;
}
