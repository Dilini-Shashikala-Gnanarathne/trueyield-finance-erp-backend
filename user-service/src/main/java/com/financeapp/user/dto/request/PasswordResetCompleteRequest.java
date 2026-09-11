package com.financeapp.user.dto.request;

import jakarta.validation.constraints.*;

/**
 * Password reset completion request.
 * Supports two verification methods:
 *  - OTP: requires txnId + otp fields
 *  - OLD_PASSWORD: requires oldPassword field
 *
 * Cross-field validation enforced in AuthService business logic.
 */
public record PasswordResetCompleteRequest(

    @NotBlank(message = "identifier (mobile) is required")
    @Pattern(regexp = "^07[01245678][0-9]{7}$", message = "Identifier must be a valid mobile number")
    String identifier,

    @NotBlank(message = "verificationMethod is required")
    String verificationMethod,  // "OTP" or "OLD_PASSWORD"

    String txnId,       // Required when verificationMethod = OTP
    String otp,         // Required when verificationMethod = OTP
    String oldPassword, // Required when verificationMethod = OLD_PASSWORD

    @NotBlank(message = "newPassword is required")
    @Pattern(
        regexp = "^(?=.*[a-z])[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{}';:\"\\\\|,.<>\\/?]{8,20}$",
        message = "New password must be 8-20 characters with at least one lowercase letter"
    )
    String newPassword
) {}
