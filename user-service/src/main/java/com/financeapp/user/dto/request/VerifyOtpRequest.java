package com.financeapp.user.dto.request;
import jakarta.validation.constraints.NotBlank;
public record VerifyOtpRequest(
    @NotBlank(message = "Transaction ID is required") String txnId,
    @NotBlank(message = "OTP is required") String otp
) {}
