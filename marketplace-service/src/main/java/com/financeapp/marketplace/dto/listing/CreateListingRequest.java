package com.financeapp.marketplace.dto.listing;

import com.financeapp.marketplace.domain.enums.QualityGrade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateListingRequest {

    @NotBlank(message = "Produce ID is required")
    private String produceId;

    private String title;

    private String description;

    @NotNull(message = "Total quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal totalQuantity;

    @NotBlank(message = "Unit of measurement is required (e.g. KG)")
    private String unit;

    @NotNull(message = "Price per unit is required")
    @DecimalMin(value = "0.01", message = "Price per unit must be greater than zero")
    private BigDecimal pricePerUnit;

    @NotNull(message = "Quality grade is required (PREMIUM, STANDARD, PROCESSING)")
    private QualityGrade qualityGrade;

    @NotNull(message = "Harvest date is required")
    private LocalDate harvestDate;

    @Valid
    @NotNull(message = "Location information is required")
    private ListingLocationDto location;

    private BigDecimal minOrderQuantity;

    private List<String> imageUrls;
}
