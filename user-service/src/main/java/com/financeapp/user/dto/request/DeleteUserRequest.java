package com.financeapp.user.dto.request;
import jakarta.validation.constraints.NotBlank;
public record DeleteUserRequest(
    @NotBlank(message = "userId is required") String userId,
    @NotBlank(message = "password is required") String password
) {}
