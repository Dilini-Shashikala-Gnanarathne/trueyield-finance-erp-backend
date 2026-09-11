package com.accmaster.usersvc.dto.request;
import jakarta.validation.constraints.NotBlank;
public record UpdateStatusRequest(
    @NotBlank(message = "userId is required") String userId,
    @NotBlank(message = "status is required") String status
) {}
