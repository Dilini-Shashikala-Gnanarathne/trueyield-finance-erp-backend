package com.financeapp.user.dto.response;
/** Returned when login or registration requires OTP verification. */
public record OtpChallengeResponse(String txnId, String message) {}
