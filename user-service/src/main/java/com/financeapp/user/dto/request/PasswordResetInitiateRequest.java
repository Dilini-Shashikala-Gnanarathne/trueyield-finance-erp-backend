package com.financeapp.user.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
public record PasswordResetInitiateRequest(
    @NotBlank(message = "identifier (mobile) is required")
    @Pattern(regexp = "^07[01245678][0-9]{7}$", message = "Identifier must be a valid mobile number")
    String identifier,
    @NotBlank(message = "action is required") String action,
    @NotBlank(message = "verificationMethod is required") String verificationMethod
) {}
