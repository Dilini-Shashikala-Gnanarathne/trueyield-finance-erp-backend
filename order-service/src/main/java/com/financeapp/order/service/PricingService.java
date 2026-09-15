package com.financeapp.order.service;

import com.financeapp.order.dto.order.PriceBreakdownDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pricing Engine fulfilling:
 * PRICE-001: Farmer Sets Price (BigDecimal for monetary values, 2 decimal places).
 * PRICE-002: Calculate Order Total (line totals and order total).
 * PRICE-003: Price Breakdown (Farmer subtotal + platform fee + delivery fee + total).
 */
@Service
public class PricingService {

    private final BigDecimal platformFeePercentage;
    private final String defaultCurrency;

    public PricingService(
            @Value("${pricing.platform-fee-percentage:5.00}") BigDecimal platformFeePercentage,
            @Value("${pricing.currency:LKR}") String defaultCurrency) {
        this.platformFeePercentage = platformFeePercentage;
        this.defaultCurrency = defaultCurrency;
    }

    public PriceBreakdownDto calculatePrice(BigDecimal quantity, BigDecimal unitPrice, String unit, boolean includeDelivery) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }

        // PRICE-001 & PRICE-002: Line total with RoundingMode.HALF_UP
        BigDecimal subtotal = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

        // PRICE-003: Platform fee (e.g. 5%)
        BigDecimal platformRate = platformFeePercentage.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        BigDecimal platformFee = subtotal.multiply(platformRate).setScale(2, RoundingMode.HALF_UP);

        // PRICE-003: Delivery fee (Standard flat agricultural delivery fee if requested, 0 if farm pickup)
        BigDecimal deliveryFee = includeDelivery
                ? new BigDecimal("350.00").setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // Order Total
        BigDecimal totalAmount = subtotal.add(platformFee).add(deliveryFee).setScale(2, RoundingMode.HALF_UP);

        return PriceBreakdownDto.builder()
                .quantity(quantity)
                .unit(unit)
                .unitPrice(unitPrice.setScale(2, RoundingMode.HALF_UP))
                .subtotal(subtotal)
                .platformFee(platformFee)
                .deliveryFee(deliveryFee)
                .totalAmount(totalAmount)
                .currency(defaultCurrency)
                .build();
    }
}
