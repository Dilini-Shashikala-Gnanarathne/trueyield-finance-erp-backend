package com.financeapp.auth.controller;

import com.financeapp.auth.dto.request.LoginRequest;
import com.financeapp.auth.dto.request.RegisterBuyerRequest;
import com.financeapp.auth.dto.request.RegisterFarmerRequest;
import com.financeapp.auth.dto.response.ApiResponse;
import com.financeapp.auth.dto.response.AuthResponse;
import com.financeapp.auth.security.SecurityPrincipal;
import com.financeapp.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & Access Control", description = "Endpoints for Farmer & Buyer Registration, Login, and RBAC")
public class AuthController {

    private final AuthService authService;

    /**
     * AUTH-001: Farmer Registration.
     */
    @PostMapping("/register/farmer")
    @Operation(summary = "Register a new Farmer account", description = "Creates farmer credentials and initial marketplace profile")
    public ResponseEntity<ApiResponse<AuthResponse>> registerFarmer(@Valid @RequestBody RegisterFarmerRequest request) {
        AuthResponse response = authService.registerFarmer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Farmer registered successfully", response));
    }

    /**
     * AUTH-002: Buyer Registration.
     */
    @PostMapping("/register/buyer")
    @Operation(summary = "Register a new Buyer account", description = "Creates buyer credentials and initial buyer profile")
    public ResponseEntity<ApiResponse<AuthResponse>> registerBuyer(@Valid @RequestBody RegisterBuyerRequest request) {
        AuthResponse response = authService.registerBuyer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Buyer registered successfully", response));
    }

    /**
     * AUTH-003: Login.
     */
    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates using phone or email and password, returning RS256 JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    // =========================================================================
    // AUTH-004: RBAC Role Verification Endpoints
    // =========================================================================

    @GetMapping("/test/farmer")
    @PreAuthorize("hasRole('FARMER')")
    @Operation(summary = "Farmer RBAC Check", description = "Verifies caller has ROLE_FARMER")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testFarmerRole(@AuthenticationPrincipal SecurityPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Farmer access verified", Map.of(
                "userId", principal.getUserId(),
                "role", principal.getRole().name()
        )));
    }

    @GetMapping("/test/buyer")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Buyer RBAC Check", description = "Verifies caller has ROLE_BUYER")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testBuyerRole(@AuthenticationPrincipal SecurityPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Buyer access verified", Map.of(
                "userId", principal.getUserId(),
                "role", principal.getRole().name()
        )));
    }

    @GetMapping("/test/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin RBAC Check", description = "Verifies caller has ROLE_ADMIN")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testAdminRole(@AuthenticationPrincipal SecurityPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Admin access verified", Map.of(
                "userId", principal.getUserId(),
                "role", principal.getRole().name()
        )));
    }
}
