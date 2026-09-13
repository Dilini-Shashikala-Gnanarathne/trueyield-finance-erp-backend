package com.financeapp.marketplace.dto.listing;

import com.financeapp.marketplace.domain.enums.QualityGrade;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateListingRequest {

    private String title;

    private String description;

    private BigDecimal totalQuantity;

    private BigDecimal pricePerUnit;

    private QualityGrade qualityGrade; // Only modifiable if DRAFT

    private LocalDate harvestDate;

    @Valid
    private ListingLocationDto location;

    private BigDecimal minOrderQuantity;
}
