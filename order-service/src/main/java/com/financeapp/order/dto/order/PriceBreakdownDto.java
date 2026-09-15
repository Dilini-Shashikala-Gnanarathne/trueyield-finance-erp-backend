package com.financeapp.order.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * PRICE-001, PRICE-002, PRICE-003:
 * Transparent pricing breakdown with high-precision BigDecimal arithmetic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceBreakdownDto {
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private BigDecimal platformFee;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private String currency;
}
