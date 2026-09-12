package com.financeapp.auth.dto.response;

import com.financeapp.auth.domain.enums.LocationVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Publicly viewable seller profile on marketplace listings (Section 5 & AUTH-005).
 * Honors location visibility privacy rules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerPublicProfileResponse {
    private String sellerId;
    private String sellerName;
    private String farmName;
    private String bio;
    private String locality;
    private String district;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocationVisibility locationVisibility;
    private String avatarUrl;
}
