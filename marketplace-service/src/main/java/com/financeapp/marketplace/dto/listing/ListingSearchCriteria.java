package com.financeapp.marketplace.dto.listing;

import com.financeapp.marketplace.domain.enums.ProduceCategory;
import com.financeapp.marketplace.domain.enums.QualityGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingSearchCriteria {

    /**
     * DISC-002: Keyword query across listing title, description, and produce name/code.
     */
    private String query;

    /**
     * DISC-003: Filter by produce ID or Code.
     */
    private String produceId;
    private String produceCode;

    /**
     * DISC-003: Filter by produce category.
     */
    private ProduceCategory category;

    /**
     * DISC-003: Locality and district filters.
     */
    private String locality;
    private String district;

    /**
     * DISC-003: Price range filters.
     */
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    /**
     * DISC-003: Quality grade filter (e.g. GRADE_A, GRADE_B).
     */
    private QualityGrade qualityGrade;

    /**
     * DISC-003: Filter listings where availableQuantity > 0. Defaults to true.
     */
    @Builder.Default
    private Boolean inStockOnly = true;

    /**
     * DISC-004: Sort option: PRICE_ASC, PRICE_DESC, NEWEST, HARVEST_DATE_ASC, HARVEST_DATE_DESC.
     */
    @Builder.Default
    private String sortBy = "NEWEST";

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;
}
