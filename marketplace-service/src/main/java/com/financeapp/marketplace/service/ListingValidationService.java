package com.financeapp.marketplace.service;

import com.financeapp.marketplace.domain.entity.ListingEntity;
import com.financeapp.marketplace.domain.entity.ProduceEntity;
import com.financeapp.marketplace.dto.listing.ValidationResultResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates business rules before listing publication (MARKET-003).
 * Agricultural and geographic domain rule validation.
 */
@Service
@Slf4j
public class ListingValidationService {

    public ValidationResultResponse validate(ListingEntity listing) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 1. Produce existence & active check
        ProduceEntity produce = listing.getProduce();
        if (produce == null) {
            errors.add("Produce type must be specified.");
        } else {
            if (Boolean.FALSE.equals(produce.getIsActive())) {
                errors.add("Produce type '" + produce.getName() + "' is inactive in catalogue.");
            }

            // 2. Unit check
            if (listing.getUnit() == null || listing.getUnit().isBlank()) {
                errors.add("Unit of measurement must be specified.");
            } else {
                List<String> supportedUnits = produce.getSupportedUnitsList();
                boolean matchesUnit = supportedUnits.stream()
                        .anyMatch(u -> u.equalsIgnoreCase(listing.getUnit()));
                if (!matchesUnit) {
                    errors.add("Unit '" + listing.getUnit() + "' is not supported for produce '" + produce.getName() + "'. Supported units: " + supportedUnits);
                }
            }

            // 3. Quality grade check
            if (listing.getQualityGrade() == null) {
                errors.add("Quality grade must be specified.");
            } else {
                List<String> supportedGrades = produce.getSupportedGradesList();
                boolean matchesGrade = supportedGrades.stream()
                        .anyMatch(g -> g.equalsIgnoreCase(listing.getQualityGrade().name()));
                if (!matchesGrade) {
                    errors.add("Quality grade '" + listing.getQualityGrade() + "' is not recognized for produce '" + produce.getName() + "'.");
                }
            }
        }

        // 4. Quantity validations
        if (listing.getTotalQuantity() == null || listing.getTotalQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Total quantity must be strictly greater than zero.");
        }
        if (listing.getAvailableQuantity() == null || listing.getAvailableQuantity().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Available quantity cannot be negative.");
        }
        if (listing.getTotalQuantity() != null && listing.getAvailableQuantity() != null
                && listing.getAvailableQuantity().compareTo(listing.getTotalQuantity()) > 0) {
            errors.add("Available quantity cannot exceed total quantity.");
        }

        // 5. Price validations
        if (listing.getPricePerUnit() == null || listing.getPricePerUnit().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Price per unit must be strictly greater than zero.");
        }

        // 6. Harvest date validation (fresh agricultural produce)
        LocalDate today = LocalDate.now();
        if (listing.getHarvestDate() == null) {
            errors.add("Harvest date must be specified.");
        } else {
            // Fresh produce cannot have been harvested more than 14 days ago
            if (listing.getHarvestDate().isBefore(today.minusDays(14))) {
                errors.add("Harvest date cannot be more than 14 days in the past (" + listing.getHarvestDate() + ").");
            }
            // Cannot be planned more than 45 days in advance
            if (listing.getHarvestDate().isAfter(today.plusDays(45))) {
                errors.add("Harvest date cannot be more than 45 days in the future (" + listing.getHarvestDate() + ").");
            }
        }

        // 7. Geographic location validations (Section 5)
        if (listing.getLatitude() == null) {
            errors.add("Location latitude is required.");
        } else if (listing.getLatitude().compareTo(new BigDecimal("-90.0")) < 0
                || listing.getLatitude().compareTo(new BigDecimal("90.0")) > 0) {
            errors.add("Latitude must be between -90.0 and 90.0.");
        }

        if (listing.getLongitude() == null) {
            errors.add("Location longitude is required.");
        } else if (listing.getLongitude().compareTo(new BigDecimal("-180.0")) < 0
                || listing.getLongitude().compareTo(new BigDecimal("180.0")) > 0) {
            errors.add("Longitude must be between -180.0 and 180.0.");
        }

        if (listing.getLocality() == null || listing.getLocality().isBlank()) {
            errors.add("Locality/area must not be blank.");
        }

        // 8. Description & images warnings/validations
        if (listing.getDescription() == null || listing.getDescription().trim().length() < 10) {
            warnings.add("Detailed produce description is recommended for better buyer discovery.");
        }

        if (listing.getImages() == null || listing.getImages().isEmpty()) {
            warnings.add("Adding at least one photograph of the produce increases buyer trust.");
        }

        boolean isValid = errors.isEmpty();
        return ValidationResultResponse.builder()
                .valid(isValid)
                .errors(errors)
                .warnings(warnings)
                .build();
    }
}
