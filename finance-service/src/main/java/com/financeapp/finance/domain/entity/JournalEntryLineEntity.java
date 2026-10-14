package com.financeapp.finance.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA Entity representing a single line (debit or credit) in a journal entry.
 */
@Entity
@Table(name = "journal_entry_line")
@Getter
@Setter
@NoArgsConstructor
public class JournalEntryLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntryEntity journalEntry;

    /**
     * Line type: DEBIT or CREDIT.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "line_type", nullable = false, length = 10)
    private LineType lineType;

    /**
     * Account code (e.g., "SALARY_EXPENSE", "PAYROLL_PAYABLE").
     */
    @Column(name = "account_code", nullable = false, length = 100)
    private String accountCode;

    /**
     * Human-readable account name for display purposes.
     */
    @Column(name = "account_name", nullable = false, length = 200)
    private String accountName;

    /**
     * Amount for this line. NUMERIC(19,4) for exact decimal precision.
     */
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /**
     * Optional description for this specific line.
     */
    @Column(name = "description", length = 300)
    private String description;

    public enum LineType {
        DEBIT,
        CREDIT
    }
}
