package com.financeapp.marketplace.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListingStatsDto {
    private long totalListings;
    private long activeListings;
    private long soldOutListings;
    private long draftListings;
}
