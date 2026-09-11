package com.financeapp.user.controller;

import com.financeapp.user.dto.request.CreateAdminRequest;
import com.financeapp.user.dto.request.DeleteUserRequest;
import com.financeapp.user.dto.request.UpdateAdminRequest;
import com.financeapp.user.dto.request.UpdateStatusRequest;
import com.financeapp.user.dto.response.AdminResponse;
import com.financeapp.user.dto.response.ApiResponse;
import com.financeapp.user.dto.response.PagedResponse;
import com.financeapp.user.security.SecurityPrincipal;
import com.financeapp.user.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
@Tag(name = "Administrator Management", description = "Endpoints for managing admin accounts (Super Admin operations)")
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a new administrator account")
    public ResponseEntity<ApiResponse<AdminResponse>> createAdmin(
            @Valid @RequestBody CreateAdminRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        AdminResponse response = adminService.createAdmin(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Administrator account created successfully.", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #id == principal.userId")
    @Operation(summary = "Get administrator by User ID")
    public ResponseEntity<ApiResponse<AdminResponse>> getAdminById(@PathVariable String id) {
        AdminResponse response = adminService.getAdminById(id);
        return ResponseEntity.ok(ApiResponse.ok("Admin profile retrieved successfully.", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "List all administrators with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<AdminResponse>>> listAdmins(
            @PageableDefault(size = 20, sort = "fname", direction = Sort.Direction.ASC) Pageable pageable) {
        PagedResponse<AdminResponse> response = adminService.listAdmins(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Administrators retrieved successfully.", response));
    }

    @PutMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or #request.id() == principal.userId")
    @Operation(summary = "Update administrator profile details")
    public ResponseEntity<ApiResponse<AdminResponse>> updateAdmin(
            @Valid @RequestBody UpdateAdminRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        AdminResponse response = adminService.updateAdmin(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Admin profile updated successfully.", response));
    }

    @PutMapping("/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update administrator account status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        adminService.updateStatus(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Admin status updated successfully."));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Soft delete administrator account")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(
            @Valid @RequestBody DeleteUserRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        adminService.deleteAdmin(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Administrator account deleted successfully."));
    }
}
