package com.financeapp.marketplace.dto.listing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerSummaryDto {
    private String sellerId;
    private String sellerName;
    private String farmName;
    private String locality;
    private String district;
    private String avatarUrl;
}
