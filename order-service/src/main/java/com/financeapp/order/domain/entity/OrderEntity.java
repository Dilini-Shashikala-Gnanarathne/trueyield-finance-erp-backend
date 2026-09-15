package com.financeapp.order.domain.entity;

import com.financeapp.order.domain.enums.OrderStatus;
import com.financeapp.order.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

    @Id
    private String id;

    @Column(name = "order_number", nullable = false, unique = true, length = 32)
    private String orderNumber;

    @Column(name = "buyer_id", nullable = false, length = 36)
    private String buyerId;

    @Column(name = "farmer_id", nullable = false, length = 36)
    private String farmerId;

    @Column(name = "listing_id", nullable = false, length = 36)
    private String listingId;

    @Column(name = "produce_name", nullable = false, length = 150)
    private String produceName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "platform_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal platformFee = BigDecimal.ZERO;

    @Column(name = "delivery_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "LKR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "buyer_notes", columnDefinition = "TEXT")
    private String buyerNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

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

    // Domain state machine transitions (Section 10)

    public void accept() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException("Cannot accept order in status " + this.status + ". Only PENDING orders can be accepted.");
        }
        this.status = OrderStatus.ACCEPTED;
    }

    public void reject(String reason) {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException("Cannot reject order in status " + this.status + ". Only PENDING orders can be rejected.");
        }
        this.status = OrderStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public void complete() {
        if (this.status != OrderStatus.ACCEPTED && this.status != OrderStatus.FULFILLED) {
            throw new BusinessException("Cannot complete order in status " + this.status);
        }
        this.status = OrderStatus.COMPLETED;
    }
}
