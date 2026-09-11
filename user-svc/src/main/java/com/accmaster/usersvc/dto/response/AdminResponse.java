package com.accmaster.usersvc.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminResponse(
    String id, String username,
    String mobile, String email, String nic,
    String firstName, String lastName, String role,
    String status, String profileUrl,
    Instant registeredAt, Instant lastUpdatedAt, Instant lastPasswordResetAt
) {}
