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
public class ProduceSalesDto {
    private String produceName;
    private BigDecimal totalQuantitySold;
    private BigDecimal totalRevenue;
}
