package com.accmaster.usersvc.dto.request;

import jakarta.validation.constraints.*;

/**
 * Student self-registration request DTO.
 * Validation patterns sourced directly from PHP StudentService::validate().
 */
public record RegisterStudentRequest(

    @NotBlank(message = "First name is required")
    @Size(max = 20, message = "First name must not exceed 20 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 20, message = "Last name must not exceed 20 characters")
    String lastName,

    @NotBlank(message = "Mobile number is required")
    @Pattern(
        regexp = "^07[01245678][0-9]{7}$",
        message = "Mobile number must be a valid 10-digit Sri Lankan mobile number (e.g. 0771234567)"
    )
    String mobile,

    @NotBlank(message = "NIC is required")
    @Pattern(
        regexp = "^(([5-9][0-9][01235678][0-9]{6}[vVxX])|([12][0-9]{3}[01235678][0-9]{7}))$",
        message = "NIC must be a valid Sri Lankan NIC (old 9-digit+V/X or new 12-digit format)"
    )
    String nic,

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{}';:\"\\\\|,.<>\\/?]{8,20}$",
        message = "Password must be 8-20 characters, contain at least one lowercase letter, and may include letters, numbers, and special characters"
    )
    String password
) {}
