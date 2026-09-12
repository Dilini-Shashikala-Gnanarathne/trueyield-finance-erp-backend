package com.financeapp.auth.dto.response;

import com.financeapp.auth.domain.enums.LocationVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * AUTH-005: Farmer Profile Response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerProfileResponse {
    private String profileId;
    private UserSummary user;
    private String farmName;
    private String bio;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locality;
    private String district;
    private LocationVisibility locationVisibility;
    private Instant locationUpdatedAt;
    private String avatarUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
