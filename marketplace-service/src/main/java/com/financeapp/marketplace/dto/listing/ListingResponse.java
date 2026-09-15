package com.financeapp.marketplace.dto.listing;

import com.financeapp.marketplace.domain.enums.ListingStatus;
import com.financeapp.marketplace.domain.enums.QualityGrade;
import com.financeapp.marketplace.dto.produce.ProduceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingResponse {

    private String id;
    private String farmerId;
    private SellerSummaryDto seller;
    private ProduceResponse produce;
    private String title;
    private String description;
    private BigDecimal totalQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal reservedQuantity;
    private String unit;
    private BigDecimal pricePerUnit;
    private QualityGrade qualityGrade;
    private LocalDate harvestDate;
    private ListingStatus status;
    private BigDecimal minOrderQuantity;
    private ListingLocationDto location;
    private String cancellationReason;
    private Instant publishedAt;
    private Instant cancelledAt;
    private List<ListingImageResponse> images;
    private Instant createdAt;
    private Instant updatedAt;
}
