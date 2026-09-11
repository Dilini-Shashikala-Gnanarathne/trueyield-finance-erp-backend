package com.accmaster.usersvc.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record UpdateAdminRequest(
    @NotBlank(message = "id is required") String id,
    @NotBlank(message = "firstName is required") @Size(max = 20) String firstName,
    @NotBlank(message = "lastName is required") @Size(max = 20) String lastName,
    String profileUrl
) {}
