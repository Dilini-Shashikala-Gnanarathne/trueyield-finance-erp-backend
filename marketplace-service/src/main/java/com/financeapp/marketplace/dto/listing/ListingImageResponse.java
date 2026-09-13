package com.financeapp.marketplace.dto.listing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingImageResponse {
    private String id;
    private String imageUrl;
    private String storageKey;
    private Boolean isPrimary;
    private Integer displayOrder;
    private Instant createdAt;
}
