package com.financeapp.user.controller;

import com.financeapp.user.dto.request.LoginRequest;
import com.financeapp.user.dto.request.PasswordResetCompleteRequest;
import com.financeapp.user.dto.request.PasswordResetInitiateRequest;
import com.financeapp.user.dto.request.RegisterStudentRequest;
import com.financeapp.user.dto.request.VerifyOtpRequest;
import com.financeapp.user.dto.response.ApiResponse;
import com.financeapp.user.dto.response.AuthResponse;
import com.financeapp.user.dto.response.OtpChallengeResponse;
import com.financeapp.user.security.SecurityPrincipal;
import com.financeapp.user.service.AuthService;
import com.financeapp.user.strategy.LoginStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & Onboarding", description = "Endpoints for login, registration, OTP validation, and password reset")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Student self-registration (triggers OTP SMS)")
    public ResponseEntity<ApiResponse<OtpChallengeResponse>> register(@Valid @RequestBody RegisterStudentRequest request) {
        OtpChallengeResponse response = authService.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration initiated successfully. Please verify OTP.", response));
    }

    @PostMapping("/register/verify")
    @Operation(summary = "Verify student registration OTP and activate account")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyRegistrationOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyRegistrationOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Registration verified successfully.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "User login (returns 2FA OTP challenge or Direct JWT)")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        LoginStrategy.LoginResult result = authService.login(request);
        if (result instanceof LoginStrategy.OtpResult otpResult) {
            return ResponseEntity.ok(ApiResponse.ok("Verification required.", otpResult.challenge()));
        } else if (result instanceof LoginStrategy.DirectResult directResult) {
            return ResponseEntity.ok(ApiResponse.ok("Login successful.", directResult.authResponse()));
        }
        return ResponseEntity.ok(ApiResponse.ok("Processing completed."));
    }

    @PostMapping("/login/verify")
    @Operation(summary = "Verify 2FA login OTP and receive JWT access token")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyLoginOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyLoginOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", response));
    }

    @PostMapping("/password/initiate")
    @Operation(summary = "Initiate password reset (OTP or old password)")
    public ResponseEntity<ApiResponse<OtpChallengeResponse>> initiatePasswordReset(
            @Valid @RequestBody PasswordResetInitiateRequest request) {
        OtpChallengeResponse response = authService.initiatePasswordReset(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset challenge initiated.", response));
    }

    @PostMapping("/password/complete")
    @Operation(summary = "Complete password reset")
    public ResponseEntity<ApiResponse<Void>> completePasswordReset(
            @Valid @RequestBody PasswordResetCompleteRequest request) {
        authService.completePasswordReset(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully."));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(
            @AuthenticationPrincipal SecurityPrincipal principal) {
        AuthResponse response = authService.getCurrentUser(principal);
        return ResponseEntity.ok(ApiResponse.ok("User profile retrieved.", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Stateless logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully."));
    }
}
