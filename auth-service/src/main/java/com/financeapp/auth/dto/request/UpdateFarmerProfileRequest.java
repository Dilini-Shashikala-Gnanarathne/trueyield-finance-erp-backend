package com.financeapp.auth.dto.request;

import com.financeapp.auth.domain.enums.LocationVisibility;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * AUTH-005: Update Farmer Profile Request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFarmerProfileRequest {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    private String farmName;

    private String bio;

    private String locality;

    private String district;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private LocationVisibility locationVisibility;

    private String avatarUrl;
}
