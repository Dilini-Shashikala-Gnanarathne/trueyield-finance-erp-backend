package com.financeapp.marketplace.dto.produce;

import com.financeapp.marketplace.domain.enums.ProduceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProduceRequest {

    @NotBlank(message = "Produce code is required (e.g. RAMBUTAN)")
    private String code;

    @NotBlank(message = "Produce name is required (e.g. Rambutan)")
    private String name;

    private String scientificName;

    @NotNull(message = "Produce category is required")
    private ProduceCategory category;

    private String description;

    @NotBlank(message = "Default unit is required (e.g. KG)")
    private String defaultUnit;

    private List<String> supportedUnits;

    private List<String> supportedGrades;

    private String imageUrl;
}
