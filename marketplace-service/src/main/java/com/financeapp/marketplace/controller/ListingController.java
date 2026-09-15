package com.financeapp.marketplace.controller;

import com.financeapp.marketplace.domain.enums.ListingStatus;
import com.financeapp.marketplace.dto.ApiResponse;
import com.financeapp.marketplace.dto.listing.*;
import com.financeapp.marketplace.security.SecurityPrincipal;
import com.financeapp.marketplace.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
@Tag(name = "Produce Listings", description = "Endpoints for listing lifecycle, validation, images, and discovery (MARKET-002 to MARKET-007)")
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    @Operation(summary = "Create a new produce listing in DRAFT state (MARKET-002)")
    public ResponseEntity<ApiResponse<ListingResponse>> createListing(
            @Valid @RequestBody CreateListingRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        ListingResponse created = listingService.createListing(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Listing created in DRAFT state.", created));
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Validate listing business rules before publication (MARKET-003)")
    public ResponseEntity<ApiResponse<ValidationResultResponse>> validateListing(@PathVariable String id) {
        ValidationResultResponse validation = listingService.validateListing(id);
        String message = validation.isValid() ? "Listing is valid for publication." : "Listing has validation errors.";
        return ResponseEntity.ok(ApiResponse.ok(message, validation));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    @Operation(summary = "Publish listing: transition DRAFT -> ACTIVE (MARKET-004)")
    public ResponseEntity<ApiResponse<ListingResponse>> publishListing(
            @PathVariable String id,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        ListingResponse published = listingService.publishListing(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Listing published successfully.", published));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    @Operation(summary = "Edit listing allowed fields based on state (MARKET-005)")
    public ResponseEntity<ApiResponse<ListingResponse>> editListing(
            @PathVariable String id,
            @Valid @RequestBody UpdateListingRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        ListingResponse updated = listingService.editListing(id, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Listing updated successfully.", updated));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    @Operation(summary = "Safely cancel a listing (MARKET-006)")
    public ResponseEntity<ApiResponse<ListingResponse>> cancelListing(
            @PathVariable String id,
            @RequestBody(required = false) CancelListingRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        ListingResponse cancelled = listingService.cancelListing(id, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Listing cancelled.", cancelled));
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    @Operation(summary = "Add image object-storage reference to listing (MARKET-007)")
    public ResponseEntity<ApiResponse<ListingImageResponse>> addImage(
            @PathVariable String id,
            @Valid @RequestBody AddImageRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        ListingImageResponse image = listingService.addImage(id, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Image reference added.", image));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    @Operation(summary = "Remove image reference from listing (MARKET-007)")
    public ResponseEntity<ApiResponse<Void>> removeImage(
            @PathVariable String id,
            @PathVariable String imageId,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        listingService.removeImage(id, imageId, principal);
        return ResponseEntity.ok(ApiResponse.ok("Image removed successfully."));
    }

    @PatchMapping("/{id}/images/{imageId}/primary")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    @Operation(summary = "Set image as primary thumbnail (MARKET-007)")
    public ResponseEntity<ApiResponse<Void>> setPrimaryImage(
            @PathVariable String id,
            @PathVariable String imageId,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        listingService.setPrimaryImage(id, imageId, principal);
        return ResponseEntity.ok(ApiResponse.ok("Primary image updated."));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get listing details by ID (Public with location privacy fuzzed for approximate view)")
    public ResponseEntity<ApiResponse<ListingResponse>> getListingById(
            @PathVariable String id,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        ListingResponse listing = listingService.getListingById(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Listing details retrieved.", listing));
    }

    @GetMapping("/farmer/me")
    @PreAuthorize("hasRole('FARMER')")
    @Operation(summary = "Get authenticated farmer's own listings")
    public ResponseEntity<ApiResponse<List<ListingResponse>>> getFarmerListings(
            @RequestParam(required = false) ListingStatus status,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        List<ListingResponse> listings = listingService.getFarmerListings(principal, status);
        return ResponseEntity.ok(ApiResponse.ok("Farmer listings retrieved.", listings));
    }

    @GetMapping
    @Operation(summary = "Browse and search active listings in marketplace (DISC-001, DISC-002, DISC-003, DISC-004)")
    public ResponseEntity<ApiResponse<com.financeapp.marketplace.dto.PageResponse<ListingSummaryResponse>>> searchListings(
            @ModelAttribute ListingSearchCriteria criteria) {
        com.financeapp.marketplace.dto.PageResponse<ListingSummaryResponse> response = listingService.searchListings(criteria);
        return ResponseEntity.ok(ApiResponse.ok("Active listings retrieved successfully.", response));
    }

    @PostMapping("/{id}/reserve")
    @Operation(summary = "Atomically reserve listing quantity (ORDER-003, ORDER-004)")
    public ResponseEntity<ApiResponse<StockOperationResponse>> reserveStock(
            @PathVariable String id,
            @Valid @RequestBody ReserveStockRequest request) {
        StockOperationResponse response = listingService.reserveStock(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Stock reserved successfully.", response));
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Atomically release reserved quantity back to available stock (ORDER-007)")
    public ResponseEntity<ApiResponse<StockOperationResponse>> releaseStock(
            @PathVariable String id,
            @Valid @RequestBody ReserveStockRequest request) {
        StockOperationResponse response = listingService.releaseStock(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Stock released successfully.", response));
    }
}
