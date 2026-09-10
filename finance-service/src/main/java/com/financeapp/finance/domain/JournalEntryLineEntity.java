package com.financeapp.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA Entity representing a single line (debit or credit) in a journal entry.
 *
 * <p>Double-entry bookkeeping requires that every journal entry have at least
 * two lines — one debit and one credit — with equal totals.
 * For example, a payroll journal has:
 * <pre>
 *   Line 1: DEBIT,  SALARY_EXPENSE,   2,500,000.00
 *   Line 2: CREDIT, PAYROLL_PAYABLE,  2,500,000.00
 * </pre>
 * </p>
 *
 * <p>Having a separate line entity (rather than storing debit/credit as columns on
 * the journal entry) allows for complex multi-line entries, such as splitting a
 * payroll entry across multiple cost centers or departments.</p>
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
     * In a full ERP this would be a foreign key to a chart of accounts table.
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
