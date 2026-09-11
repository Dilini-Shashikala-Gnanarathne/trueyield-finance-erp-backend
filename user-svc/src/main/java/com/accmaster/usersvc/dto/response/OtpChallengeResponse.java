package com.accmaster.usersvc.dto.response;
/** Returned when login or registration requires OTP verification. */
public record OtpChallengeResponse(String txnId, String message) {}
