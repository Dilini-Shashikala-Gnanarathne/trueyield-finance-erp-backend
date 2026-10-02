package com.financeapp.marketplace.controller;

import com.financeapp.marketplace.dto.ApiResponse;
import com.financeapp.marketplace.dto.listing.ListingResponse;
import com.financeapp.marketplace.security.SecurityPrincipal;
import com.financeapp.marketplace.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/listings")
@RequiredArgsConstructor
public class AdminListingController {

    private final ListingService listingService;

    @PatchMapping("/{listingId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ListingResponse>> adminCancelListing(
            @PathVariable String listingId,
            @RequestParam(name = "reason", required = false) String reason,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        
        ListingResponse response = listingService.adminCancelListing(listingId, reason, principal);
        return ResponseEntity.ok(ApiResponse.ok("Listing successfully cancelled by admin", response));
    }
}
