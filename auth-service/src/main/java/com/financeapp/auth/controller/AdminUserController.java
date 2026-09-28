package com.financeapp.auth.controller;

import com.financeapp.auth.domain.enums.UserStatus;
import com.financeapp.auth.dto.response.ApiResponse;
import com.financeapp.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AuthService authService;

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable String userId,
            @RequestParam UserStatus status) {
        
        authService.updateUserStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.ok("User status updated successfully", null));
    }
}
