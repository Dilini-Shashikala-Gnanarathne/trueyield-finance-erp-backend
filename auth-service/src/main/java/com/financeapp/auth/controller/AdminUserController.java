package com.financeapp.auth.controller;

import com.financeapp.auth.domain.enums.UserRole;
import com.financeapp.auth.domain.enums.UserStatus;
import com.financeapp.auth.dto.response.ApiResponse;
import com.financeapp.auth.dto.response.UserSummary;
import com.financeapp.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin – User Management", description = "Admin-only endpoints to list, view, and moderate user accounts")
public class AdminUserController {

    private final AuthService authService;

    /**
     * List all users with optional role and status filters (paginated).
     * GET /api/v1/admin/users?role=FARMER&status=ACTIVE&page=0&size=20
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users", description = "Paginated list of all users; optionally filter by role and/or status")
    public ResponseEntity<ApiResponse<Page<UserSummary>>> listUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserSummary> result = authService.listUsers(role, status, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved successfully", result));
    }

    /**
     * Get a single user by ID.
     * GET /api/v1/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Returns full summary for a single user account")
    public ResponseEntity<ApiResponse<UserSummary>> getUserById(@PathVariable String userId) {
        UserSummary user = authService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.ok("User retrieved successfully", user));
    }

    /**
     * Update user account status (ACTIVE / SUSPENDED / DEACTIVATED).
     * PATCH /api/v1/admin/users/{userId}/status?status=SUSPENDED
     */
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status", description = "Activates, suspends, or deactivates a user account and invalidates active tokens")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable String userId,
            @RequestParam UserStatus status) {

        authService.updateUserStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.ok("User status updated successfully", null));
    }
}

