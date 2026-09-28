package com.financeapp.auth.controller;

import com.financeapp.auth.domain.enums.UserRole;
import com.financeapp.auth.dto.response.ApiResponse;
import com.financeapp.auth.dto.response.admin.UserStatsDto;
import com.financeapp.auth.repository.UserRepository;
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

    private final UserRepository userRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserStatsDto>> getUserStats() {
        long totalUsers = userRepository.count();
        long totalFarmers = userRepository.countByRole(UserRole.FARMER);
        long totalBuyers = userRepository.countByRole(UserRole.BUYER);
        long totalAdmins = userRepository.countByRole(UserRole.ADMIN);

        UserStatsDto stats = UserStatsDto.builder()
                .totalUsers(totalUsers)
                .totalFarmers(totalFarmers)
                .totalBuyers(totalBuyers)
                .totalAdmins(totalAdmins)
                .build();

        return ResponseEntity.ok(ApiResponse.ok("User stats retrieved successfully", stats));
    }
}
