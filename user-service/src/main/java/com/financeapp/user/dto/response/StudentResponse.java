package com.financeapp.user.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StudentResponse(
    String id, String academicId,
    String firstName, String lastName, String email,
    String mobile, String nic, String gender,
    String whatsappNumber, String school,
    String guardianName, String guardianMobile,
    String batchId, String batchName,
    String status, String profileUrl,
    Instant registeredAt, Instant lastUpdatedAt, Instant lastPasswordResetAt,
    String addressLine1, String addressLine2,
    Integer cityId, String cityName,
    Integer districtId, String districtName, String zipcode
) {}
