package com.financeapp.finance.domain;

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
 *
 * <p>A journal entry is the fundamental building block of double-entry bookkeeping.
 * Every financial transaction must have at least one debit and one credit,
 * and the total debits must equal total credits (the accounting equation).</p>
 *
 * <p>Example — Payroll journal:
 * <pre>
 *   Reference: PAY-2026-08-0001
 *   Description: Payroll for period 2026-08 — 25 employees
 *
 *   DEBIT:   Salary Expense      2,500,000.00
 *   CREDIT:  Payroll Payable     2,500,000.00
 * </pre>
 * </p>
 *
 * <h2>Idempotency Design</h2>
 * <p>The {@code reference} field has a database-level UNIQUE constraint.
 * This is the critical guard against duplicate journal entries:
 * <ul>
 *   <li>If gRPC succeeds on the first attempt, the journal is created.</li>
 *   <li>If the response is lost and the caller retries, the DB constraint
 *       prevents a duplicate, and the application returns the existing entry.</li>
 * </ul>
 * This makes CreateJournalEntry a safe, idempotent operation.</p>
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

    /**
     * Business reference — acts as the idempotency key.
     * Matches the payroll reference (e.g., PAY-2026-08-0001).
     * The UNIQUE constraint at the DB level is the final safety net.
     */
    @Column(name = "reference", nullable = false, unique = true, length = 100)
    private String reference;

    /**
     * Human-readable description of the transaction.
     */
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    /**
     * Entry type for classification: PAYROLL, ACCOUNTS_PAYABLE, etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 30)
    private JournalEntryType entryType;

    /**
     * ISO 4217 currency code.
     */
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /**
     * Total amount of the journal entry (sum of all debit lines = sum of all credit lines).
     * NUMERIC(19,4) for exact decimal precision.
     */
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    /**
     * Current status of the journal entry.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JournalEntryStatus status;

    /**
     * Source system that requested this journal entry (e.g., "PAYROLL-SERVICE").
     * Used for audit trail.
     */
    @Column(name = "source_system", nullable = false, length = 100)
    private String sourceSystem;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * The individual debit and credit lines of this journal entry.
     * Cascade persist ensures lines are saved when the entry is saved.
     * The one-to-many relationship allows multi-line journal entries.
     */
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
