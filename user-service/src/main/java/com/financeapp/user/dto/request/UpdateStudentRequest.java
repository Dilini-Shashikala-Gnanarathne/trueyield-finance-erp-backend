package com.financeapp.user.dto.request;

import jakarta.validation.constraints.*;

public record UpdateStudentRequest(

    @NotBlank(message = "id is required") String id,

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

    @NotBlank(message = "gender is required")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER|PREFER_NOT)$", message = "gender must be MALE, FEMALE, OTHER, or PREFER_NOT")
    String gender,

    @Pattern(regexp = "^07[01245678][0-9]{7}$", message = "WhatsApp number must be a valid 10-digit Sri Lankan mobile number")
    String whatsappNumber,

    String profileUrl,

    @Size(max = 50, message = "addressLine1 must not exceed 50 characters")
    String addressLine1,

    @Size(max = 50, message = "addressLine2 must not exceed 50 characters")
    String addressLine2,

    Integer cityId,
    String school,

    @Size(max = 100, message = "guardianName must not exceed 100 characters")
    String guardianName,

    @Pattern(regexp = "^07[01245678][0-9]{7}$", message = "Guardian mobile must be a valid 10-digit Sri Lankan mobile number")
    String guardianMobile,

    String batchId
) {}
