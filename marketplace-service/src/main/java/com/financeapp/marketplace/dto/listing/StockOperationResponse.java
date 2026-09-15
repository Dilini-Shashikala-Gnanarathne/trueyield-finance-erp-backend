package com.financeapp.marketplace.dto.listing;

import com.financeapp.marketplace.domain.enums.ListingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockOperationResponse {
    private String listingId;
    private BigDecimal reservedQuantity;
    private BigDecimal remainingAvailableQuantity;
    private ListingStatus status;
    private boolean success;
    private String message;
}
