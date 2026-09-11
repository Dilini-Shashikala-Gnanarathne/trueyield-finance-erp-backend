package com.financeapp.user.dto.request;

import jakarta.validation.constraints.*;

public record CreateAdminRequest(

    @NotBlank(message = "mobile is required")
    @Pattern(regexp = "^07[01245678][0-9]{7}$", message = "Mobile must be a valid Sri Lankan 10-digit number")
    String mobile,

    @NotBlank(message = "password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{}';:\"\\\\|,.<>\\/?]{8,20}$",
        message = "Password must be 8-20 characters with at least one lowercase letter"
    )
    String password,

    @NotBlank(message = "nic is required")
    @Pattern(
        regexp = "^(([5-9][0-9][01235678][0-9]{6}[vVxX])|([12][0-9]{3}[01235678][0-9]{7}))$",
        message = "NIC must be a valid Sri Lankan NIC"
    )
    String nic,

    @NotBlank(message = "firstName is required")
    @Size(max = 20, message = "firstName must not exceed 20 characters")
    String firstName,

    @NotBlank(message = "lastName is required")
    @Size(max = 20, message = "lastName must not exceed 20 characters")
    String lastName,

    @NotBlank(message = "role is required")
    @Pattern(regexp = "^(SUPER_ADMIN|SUB_ADMIN)$", message = "role must be SUPER_ADMIN or SUB_ADMIN")
    String role
) {}
