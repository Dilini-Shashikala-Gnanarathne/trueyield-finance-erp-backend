package com.financeapp.auth.controller;

import com.financeapp.auth.dto.request.UpdateBuyerProfileRequest;
import com.financeapp.auth.dto.request.UpdateFarmerProfileRequest;
import com.financeapp.auth.dto.response.ApiResponse;
import com.financeapp.auth.dto.response.BuyerProfileResponse;
import com.financeapp.auth.dto.response.FarmerProfileResponse;
import com.financeapp.auth.dto.response.SellerPublicProfileResponse;
import com.financeapp.auth.security.SecurityPrincipal;
import com.financeapp.auth.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile Management", description = "Endpoints for managing farmer, buyer, and marketplace seller profile info (AUTH-005)")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * AUTH-005: Get authenticated user's profile.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current profile", description = "Returns full profile for the authenticated farmer, buyer, or admin")
    public ResponseEntity<ApiResponse<Object>> getMyProfile(@AuthenticationPrincipal SecurityPrincipal principal) {
        Object profile = profileService.getMyProfile(principal);
        return ResponseEntity.ok(ApiResponse.ok("Profile retrieved successfully", profile));
    }

    /**
     * AUTH-005: Update farmer profile.
     */
    @PutMapping("/farmer")
    @PreAuthorize("hasRole('FARMER')")
    @Operation(summary = "Update farmer profile", description = "Updates personal details, farm details, and seller marketplace location")
    public ResponseEntity<ApiResponse<FarmerProfileResponse>> updateFarmerProfile(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @Valid @RequestBody UpdateFarmerProfileRequest request) {
        FarmerProfileResponse response = profileService.updateFarmerProfile(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Farmer profile updated successfully", response));
    }

    /**
     * AUTH-005: Update buyer profile.
     */
    @PutMapping("/buyer")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Update buyer profile", description = "Updates personal details, delivery address, and contact preferences")
    public ResponseEntity<ApiResponse<BuyerProfileResponse>> updateBuyerProfile(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @Valid @RequestBody UpdateBuyerProfileRequest request) {
        BuyerProfileResponse response = profileService.updateBuyerProfile(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Buyer profile updated successfully", response));
    }

    /**
     * AUTH-005 & Map Section 5: Public seller profile view for marketplace listings.
     */
    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get public seller profile", description = "Public profile view for buyers on listings, honoring location privacy controls")
    public ResponseEntity<ApiResponse<SellerPublicProfileResponse>> getSellerPublicProfile(@PathVariable String sellerId) {
        SellerPublicProfileResponse response = profileService.getSellerPublicProfile(sellerId);
        return ResponseEntity.ok(ApiResponse.ok("Seller profile retrieved successfully", response));
    }
}
