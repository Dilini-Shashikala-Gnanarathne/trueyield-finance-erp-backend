package com.financeapp.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * AUTH-005: Buyer Profile Response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerProfileResponse {
    private String profileId;
    private UserSummary user;
    private String deliveryAddress;
    private String locality;
    private String district;
    private String preferredContactMethod;
    private String avatarUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
