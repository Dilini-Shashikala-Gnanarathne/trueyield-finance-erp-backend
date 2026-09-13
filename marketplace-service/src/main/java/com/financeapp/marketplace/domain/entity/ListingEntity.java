package com.financeapp.marketplace.domain.entity;

import com.financeapp.marketplace.domain.enums.ListingStatus;
import com.financeapp.marketplace.domain.enums.LocationVisibility;
import com.financeapp.marketplace.domain.enums.QualityGrade;
import com.financeapp.marketplace.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingEntity {

    @Id
    private String id;

    @Column(name = "farmer_id", nullable = false, length = 36)
    private String farmerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produce_id", nullable = false)
    private ProduceEntity produce;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalQuantity;

    @Column(name = "available_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal availableQuantity;

    @Column(name = "reserved_quantity", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "price_per_unit", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "quality_grade", nullable = false, length = 50)
    private QualityGrade qualityGrade;

    @Column(name = "harvest_date", nullable = false)
    private LocalDate harvestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ListingStatus status = ListingStatus.DRAFT;

    @Column(name = "min_order_quantity", precision = 12, scale = 2)
    private BigDecimal minOrderQuantity;

    // Location details (Section 5)
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false, length = 150)
    private String locality;

    @Column(length = 100)
    private String district;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_visibility", nullable = false, length = 30)
    @Builder.Default
    private LocationVisibility locationVisibility = LocationVisibility.APPROXIMATE;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ListingImageEntity> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    // Domain state machine transitions (Section 9)

    public void publish() {
        if (this.status != ListingStatus.DRAFT) {
            throw new BusinessException("Cannot publish listing: current status is " + this.status + ". Only DRAFT listings can be published.");
        }
        this.status = ListingStatus.ACTIVE;
        this.publishedAt = Instant.now();
    }

    public void cancel(String reason) {
        if (this.status.isTerminal()) {
            throw new BusinessException("Cannot cancel listing: already in terminal status " + this.status);
        }
        if (this.reservedQuantity != null && this.reservedQuantity.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Cannot cancel listing with active reserved orders. Reserved quantity: " + this.reservedQuantity);
        }
        this.status = ListingStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = Instant.now();
    }

    public void addImage(ListingImageEntity image) {
        images.add(image);
        image.setListing(this);
    }

    public void removeImage(ListingImageEntity image) {
        images.remove(image);
        image.setListing(null);
    }
}
