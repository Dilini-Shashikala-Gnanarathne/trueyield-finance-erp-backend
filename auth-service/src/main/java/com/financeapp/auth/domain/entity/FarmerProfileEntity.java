package com.financeapp.auth.domain.entity;

import com.financeapp.auth.domain.enums.LocationVisibility;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "usr_farmer_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerProfileEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "farm_name", length = 150)
    private String farmName;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "locality", length = 100)
    private String locality;

    @Column(name = "district", length = 100)
    private String district;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_visibility", nullable = false, length = 30)
    @Builder.Default
    private LocationVisibility locationVisibility = LocationVisibility.APPROXIMATE;

    @Column(name = "location_updated_at")
    private Instant locationUpdatedAt;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
