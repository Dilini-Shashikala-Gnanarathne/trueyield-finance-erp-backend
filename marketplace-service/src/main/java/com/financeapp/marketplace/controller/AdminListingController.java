package com.financeapp.marketplace.controller;

import com.financeapp.marketplace.domain.enums.ListingStatus;
import com.financeapp.marketplace.dto.ApiResponse;
import com.financeapp.marketplace.dto.PageResponse;
import com.financeapp.marketplace.dto.listing.ListingResponse;
import com.financeapp.marketplace.dto.listing.ListingSummaryResponse;
import com.financeapp.marketplace.security.SecurityPrincipal;
import com.financeapp.marketplace.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/listings")
@RequiredArgsConstructor
@Tag(name = "Admin – Listing Management", description = "Admin-only endpoints to list, view, and moderate marketplace listings")
public class AdminListingController {

    private final ListingService listingService;

    /**
     * Paginated list of all listings — all statuses.
     * GET /api/v1/admin/listings?status=DRAFT&farmerId=xxx&page=0&size=20
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all listings", description = "Paginated listing table for admins; optionally filter by status and/or farmerId")
    public ResponseEntity<ApiResponse<PageResponse<ListingSummaryResponse>>> listListings(
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(required = false) String farmerId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<ListingSummaryResponse> result =
                listingService.adminListListings(status, farmerId, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Listings retrieved successfully", result));
    }

    /**
     * Full detail view of any listing regardless of status.
     * GET /api/v1/admin/listings/{listingId}
     */
    @GetMapping("/{listingId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get listing by ID", description = "Returns full listing detail including exact location; no privacy fuzzing for admins")
    public ResponseEntity<ApiResponse<ListingResponse>> getListing(
            @PathVariable String listingId,
            @AuthenticationPrincipal SecurityPrincipal principal) {

        ListingResponse response = listingService.adminGetListing(listingId, principal);
        return ResponseEntity.ok(ApiResponse.ok("Listing retrieved successfully", response));
    }

    /**
     * Cancel any listing (ACTIVE or DRAFT).
     * PATCH /api/v1/admin/listings/{listingId}/cancel?reason=policy+violation
     */
    @PatchMapping("/{listingId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel listing", description = "Admin cancels an ACTIVE or DRAFT listing with an optional reason")
    public ResponseEntity<ApiResponse<ListingResponse>> adminCancelListing(
            @PathVariable String listingId,
            @RequestParam(name = "reason", required = false) String reason,
            @AuthenticationPrincipal SecurityPrincipal principal) {

        ListingResponse response = listingService.adminCancelListing(listingId, reason, principal);
        return ResponseEntity.ok(ApiResponse.ok("Listing successfully cancelled by admin", response));
    }
}

