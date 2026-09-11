package com.financeapp.user.dto.response;
import java.time.Instant;
/** Returned on successful direct authentication. */
public record AuthResponse(
    String accessToken,
    String userId,
    String role,
    String userType,
    String firstName,
    String lastName,
    String mobile,
    String email,
    String nic,
    String academicId,
    String gender,
    String profileUrl,
    boolean forcePasswordReset,
    Instant registeredAt
) {}
