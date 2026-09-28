package com.financeapp.order.controller;

import com.financeapp.order.dto.ApiResponse;
import com.financeapp.order.dto.dashboard.BuyerDashboardDto;
import com.financeapp.order.dto.dashboard.FarmerDashboardDto;
import com.financeapp.order.security.SecurityPrincipal;
import com.financeapp.order.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/farmer")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<FarmerDashboardDto>> getFarmerDashboard(
            @AuthenticationPrincipal SecurityPrincipal principal) {
        FarmerDashboardDto dashboard = dashboardService.getFarmerDashboard(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Farmer dashboard retrieved successfully", dashboard));
    }

    @GetMapping("/buyer")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<BuyerDashboardDto>> getBuyerDashboard(
            @AuthenticationPrincipal SecurityPrincipal principal) {
        BuyerDashboardDto dashboard = dashboardService.getBuyerDashboard(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Buyer dashboard retrieved successfully", dashboard));
    }
}
