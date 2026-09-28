package com.financeapp.order.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesSummaryDto {
    private String farmerId;
    private BigDecimal totalOverallRevenue;
    private List<ProduceSalesDto> salesByProduce;
}
