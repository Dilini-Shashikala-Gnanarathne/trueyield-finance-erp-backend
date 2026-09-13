package com.financeapp.marketplace.dto.listing;

import com.financeapp.marketplace.domain.enums.ListingStatus;
import com.financeapp.marketplace.domain.enums.QualityGrade;
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
public class ListingSummaryResponse {

    private String id;
    private String farmerId;
    private String produceName;
    private String produceCode;
    private String title;
    private BigDecimal availableQuantity;
    private String unit;
    private BigDecimal pricePerUnit;
    private QualityGrade qualityGrade;
    private LocalDate harvestDate;
    private ListingStatus status;
    private String locality;
    private String district;
    private String primaryImageUrl;
}
