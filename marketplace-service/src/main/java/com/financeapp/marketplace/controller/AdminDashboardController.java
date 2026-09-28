package com.financeapp.marketplace.controller;

import com.financeapp.marketplace.domain.enums.ListingStatus;
import com.financeapp.marketplace.dto.ApiResponse;
import com.financeapp.marketplace.dto.dashboard.ListingStatsDto;
import com.financeapp.marketplace.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ListingRepository listingRepository;

    @GetMapping("/listings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ListingStatsDto>> getListingStats() {
        long totalListings = listingRepository.count();
        long activeListings = listingRepository.countByStatus(ListingStatus.ACTIVE);
        long soldOutListings = listingRepository.countByStatus(ListingStatus.SOLD_OUT);
        long draftListings = listingRepository.countByStatus(ListingStatus.DRAFT);

        ListingStatsDto stats = ListingStatsDto.builder()
                .totalListings(totalListings)
                .activeListings(activeListings)
                .soldOutListings(soldOutListings)
                .draftListings(draftListings)
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Listing stats retrieved successfully", stats));
    }
}
