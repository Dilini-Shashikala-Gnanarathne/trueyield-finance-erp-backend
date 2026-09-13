package com.financeapp.marketplace.dto.produce;

import com.financeapp.marketplace.domain.enums.ProduceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProduceResponse {
    private String id;
    private String code;
    private String name;
    private String scientificName;
    private ProduceCategory category;
    private String description;
    private String defaultUnit;
    private List<String> supportedUnits;
    private List<String> supportedGrades;
    private String imageUrl;
    private Boolean isActive;
    private Instant createdAt;
}
