package com.financeapp.finance.domain.entity;

import com.financeapp.finance.domain.enums.JournalEntryStatus;
import com.financeapp.finance.domain.enums.JournalEntryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity representing a double-entry accounting journal entry.
 */
@Entity
@Table(
        name = "journal_entry",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_journal_entry_reference",
                        columnNames = "reference"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class JournalEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference", nullable = false, unique = true, length = 100)
    private String reference;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 30)
    private JournalEntryType entryType;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JournalEntryStatus status;

    @Column(name = "source_system", nullable = false, length = 100)
    private String sourceSystem;

    /** Originating marketplace order (null for non-order entries). */
    @Column(name = "order_id", length = 36)
    private String orderId;

    /** Farmer party of the order, used for farmer statements. */
    @Column(name = "farmer_id", length = 36)
    private String farmerId;

    /** Buyer party of the order, used for buyer statements. */
    @Column(name = "buyer_id", length = 36)
    private String buyerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "journalEntry",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<JournalEntryLineEntity> lines = new ArrayList<>();

    public void addLine(JournalEntryLineEntity line) {
        lines.add(line);
        line.setJournalEntry(this);
    }
}
