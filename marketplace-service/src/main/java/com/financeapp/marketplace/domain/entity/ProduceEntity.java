package com.financeapp.marketplace.domain.entity;

import com.financeapp.marketplace.domain.enums.ProduceCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "produce")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduceEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "scientific_name", length = 150)
    private String scientificName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProduceCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_unit", nullable = false, length = 20)
    private String defaultUnit;

    @Column(name = "supported_units", nullable = false, length = 200)
    private String supportedUnits;

    @Column(name = "supported_grades", nullable = false, length = 200)
    private String supportedGrades;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public List<String> getSupportedUnitsList() {
        if (supportedUnits == null || supportedUnits.isBlank()) {
            return List.of();
        }
        return Arrays.stream(supportedUnits.split(","))
                .map(String::trim)
                .toList();
    }

    public List<String> getSupportedGradesList() {
        if (supportedGrades == null || supportedGrades.isBlank()) {
            return List.of();
        }
        return Arrays.stream(supportedGrades.split(","))
                .map(String::trim)
                .toList();
    }
}
