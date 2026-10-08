package com.financeapp.finance.service;

import com.financeapp.finance.domain.*;
import com.financeapp.finance.dto.CreateJournalEntryCommand;
import com.financeapp.finance.dto.JournalEntryResult;
import com.financeapp.finance.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Application service for journal entry operations in Finance Service.
 *
 * <p>This is the core business logic layer of Finance Service.
 * It is deliberately separated from the gRPC adapter ({@link com.financeapp.finance.grpc.FinanceGrpcServer})
 * so that:
 * <ul>
 *   <li>Business logic can be unit-tested without a running gRPC server.</li>
 *   <li>The same logic is reused by the REST endpoint (for benchmark comparison).</li>
 *   <li>The transport mechanism can change without touching business logic.</li>
 * </ul>
 * </p>
 *
 * <h2>Idempotency Implementation</h2>
 * <p>The idempotency logic has two layers:
 * <ol>
 *   <li><strong>Application layer:</strong> Check for existing reference before inserting.</li>
 *   <li><strong>Database layer:</strong> UNIQUE constraint on the {@code reference} column
 *       acts as the final safety net even under concurrent requests.</li>
 * </ol>
 *
 * Why two layers?
 * The application check handles the common case efficiently (one SELECT).
 * The DB constraint handles rare race conditions where two concurrent requests
 * pass the application check simultaneously — the DB will reject the second insert,
 * and we catch the {@link DataIntegrityViolationException} and return the existing entry.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JournalEntryApplicationService {

    private final JournalEntryRepository journalEntryRepository;
    private final FinancialAuditService financialAuditService;

    /**
     * Create a journal entry, or return the existing one if the reference already exists.
     *
     * <p>This operation is <strong>idempotent</strong>: calling it multiple times with
     * the same reference always returns the same result and never creates duplicates.</p>
     *
     * @param command the journal entry creation command
     * @return the created (or existing) journal entry result
     */
    @Transactional
    @CacheEvict(value = {"trialBalance", "accountBalances"}, allEntries = true)
    public JournalEntryResult createJournalEntry(CreateJournalEntryCommand command) {
        log.info("Processing journal entry request. reference={}, amount={}, currency={}",
                command.getReference(), command.getAmount(), command.getCurrency());

        // Application-layer idempotency check
        // Check if a journal with this reference already exists
        return journalEntryRepository.findByReference(command.getReference())
                .map(existing -> {
                    log.info("Idempotent return: journal entry already exists. reference={}, id={}",
                            existing.getReference(), existing.getId());
                    return buildResult(existing, true, "Journal entry already exists — idempotent return");
                })
                .orElseGet(() -> createNewJournalEntry(command));
    }

    /**
     * Posts the multi-line settlement journal for a paid marketplace order.
     *
     * <pre>
     *   DEBIT:   CASH                    total          (buyer payment received)
     *   CREDIT:  FARMER_PAYABLE          total - fees   (owed to the farmer)
     *   CREDIT:  PLATFORM_FEE_REVENUE    platformFee    (platform income)
     *   CREDIT:  DELIVERY_FEE_REVENUE    deliveryFee    (only when > 0)
     * </pre>
     *
     * Idempotent on {@code reference}, like {@link #createJournalEntry}.
     * The farmer payable is derived as total - platformFee - deliveryFee so the entry always balances.
     */
    @Transactional
    public JournalEntryResult createOrderSettlementJournal(String reference, String description,
                                                           BigDecimal total, BigDecimal platformFee,
                                                           BigDecimal deliveryFee, String currency,
                                                           String sourceSystem, String orderId,
                                                           String farmerId, String buyerId) {
        return journalEntryRepository.findByReference(reference)
                .map(existing -> buildResult(existing, true, "Journal entry already exists - idempotent return"))
                .orElseGet(() -> {
                    BigDecimal fee = platformFee != null ? platformFee : BigDecimal.ZERO;
                    BigDecimal delivery = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
                    BigDecimal farmerShare = total.subtract(fee).subtract(delivery);

                    if (total.compareTo(BigDecimal.ZERO) <= 0 || farmerShare.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("Invalid settlement amounts for " + reference
                                + ": total=" + total + ", platformFee=" + fee + ", deliveryFee=" + delivery);
                    }
                    if (currency == null || currency.length() != 3) {
                        throw new IllegalArgumentException("Currency must be a valid ISO 4217 3-letter code, got: " + currency);
                    }

                    JournalEntryEntity entry = new JournalEntryEntity();
                    entry.setReference(reference);
                    entry.setDescription(description);
                    entry.setCurrency(currency);
                    entry.setTotalAmount(total);
                    entry.setStatus(JournalEntryStatus.CREATED);
                    entry.setSourceSystem(sourceSystem != null ? sourceSystem : "UNKNOWN");
                    entry.setEntryType(JournalEntryType.GENERAL_LEDGER);
                    entry.setOrderId(orderId);
                    entry.setFarmerId(farmerId);
                    entry.setBuyerId(buyerId);

                    entry.addLine(buildLine(JournalEntryLineEntity.LineType.DEBIT, "CASH", total, description));
                    if (farmerShare.compareTo(BigDecimal.ZERO) > 0) {
                        entry.addLine(buildLine(JournalEntryLineEntity.LineType.CREDIT, "FARMER_PAYABLE", farmerShare, description));
                    }
                    if (fee.compareTo(BigDecimal.ZERO) > 0) {
                        entry.addLine(buildLine(JournalEntryLineEntity.LineType.CREDIT, "PLATFORM_FEE_REVENUE", fee, description));
                    }
                    if (delivery.compareTo(BigDecimal.ZERO) > 0) {
                        entry.addLine(buildLine(JournalEntryLineEntity.LineType.CREDIT, "DELIVERY_FEE_REVENUE", delivery, description));
                    }

                    try {
                        JournalEntryEntity saved = journalEntryRepository.save(entry);
                        financialAuditService.recordEntryAudit(saved, "CREATED",
                                sourceSystem != null ? sourceSystem : "SYSTEM");
                        log.info("Settlement journal created. reference={}, id={}, total={}",
                                saved.getReference(), saved.getId(), saved.getTotalAmount());
                        return buildResult(saved, false, "Settlement journal created successfully");
                    } catch (DataIntegrityViolationException e) {
                        log.warn("Concurrent idempotency on settlement journal. reference={}", reference);
                        return journalEntryRepository.findByReference(reference)
                                .map(existing -> buildResult(existing, true,
                                        "Journal entry created by concurrent request - idempotent return"))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Journal entry reference constraint violation but entry not found: " + reference));
                    }
                });
    }

    private JournalEntryLineEntity buildLine(JournalEntryLineEntity.LineType type, String accountCode,
                                             BigDecimal amount, String description) {
        JournalEntryLineEntity line = new JournalEntryLineEntity();
        line.setLineType(type);
        line.setAccountCode(accountCode);
        line.setAccountName(resolveAccountName(accountCode));
        line.setAmount(amount);
        line.setDescription(description);
        return line;
    }

    private JournalEntryResult createNewJournalEntry(CreateJournalEntryCommand command) {
        validateCommand(command);

        JournalEntryEntity entry = buildJournalEntry(command);

        try {
            JournalEntryEntity saved = journalEntryRepository.save(entry);
            financialAuditService.recordEntryAudit(
                    saved,
                    "CREATED",
                    command.getSourceSystem() != null ? command.getSourceSystem() : "SYSTEM"
            );
            log.info("Journal entry created successfully. reference={}, id={}, amount={}",
                    saved.getReference(), saved.getId(), saved.getTotalAmount());
            return buildResult(saved, false, "Journal entry created successfully");

        } catch (DataIntegrityViolationException e) {
            // Race condition: another concurrent request already created the entry.
            // The DB unique constraint caught what the application check missed.
            log.warn("Concurrent idempotency: journal entry created by concurrent request. reference={}",
                    command.getReference());

            return journalEntryRepository.findByReference(command.getReference())
                    .map(existing -> buildResult(existing, true,
                            "Journal entry created by concurrent request — idempotent return"))
                    .orElseThrow(() -> new IllegalStateException(
                            "Journal entry reference constraint violation but entry not found: " +
                            command.getReference()));
        }
    }

    /**
     * Validates the command before processing.
     * Additional validation beyond proto-level field presence.
     */
    private void validateCommand(CreateJournalEntryCommand command) {
        if (command.getAmount() == null || command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive, got: " + command.getAmount());
        }
        if (command.getDebitAccount().equals(command.getCreditAccount())) {
            throw new IllegalArgumentException(
                    "Debit and credit accounts cannot be the same: " + command.getDebitAccount());
        }
        if (command.getCurrency() == null || command.getCurrency().length() != 3) {
            throw new IllegalArgumentException(
                    "Currency must be a valid ISO 4217 3-letter code, got: " + command.getCurrency());
        }
    }

    /**
     * Builds a JournalEntryEntity with its debit and credit lines from the command.
     *
     * <p>This creates a standard double-entry payroll journal:
     * <pre>
     *   DEBIT:   Salary Expense      [amount]
     *   CREDIT:  Payroll Payable     [amount]
     * </pre>
     * </p>
     */
    private JournalEntryEntity buildJournalEntry(CreateJournalEntryCommand command) {
        JournalEntryEntity entry = new JournalEntryEntity();
        entry.setReference(command.getReference());
        entry.setDescription(command.getDescription());
        entry.setCurrency(command.getCurrency());
        entry.setTotalAmount(command.getAmount());
        entry.setStatus(JournalEntryStatus.CREATED);
        entry.setSourceSystem(command.getSourceSystem() != null ? command.getSourceSystem() : "UNKNOWN");
        entry.setEntryType(parseEntryType(command.getEntryType()));

        // Debit line: Salary Expense increases (debit) when payroll is processed
        JournalEntryLineEntity debitLine = new JournalEntryLineEntity();
        debitLine.setLineType(JournalEntryLineEntity.LineType.DEBIT);
        debitLine.setAccountCode(command.getDebitAccount());
        debitLine.setAccountName(resolveAccountName(command.getDebitAccount()));
        debitLine.setAmount(command.getAmount());
        debitLine.setDescription("Payroll debit: " + command.getDescription());

        // Credit line: Payroll Payable increases (credit) — the liability owed to employees
        JournalEntryLineEntity creditLine = new JournalEntryLineEntity();
        creditLine.setLineType(JournalEntryLineEntity.LineType.CREDIT);
        creditLine.setAccountCode(command.getCreditAccount());
        creditLine.setAccountName(resolveAccountName(command.getCreditAccount()));
        creditLine.setAmount(command.getAmount());
        creditLine.setDescription("Payroll credit: " + command.getDescription());

        entry.addLine(debitLine);
        entry.addLine(creditLine);

        return entry;
    }

    private JournalEntryType parseEntryType(String entryType) {
        if (entryType == null || entryType.isBlank()) {
            return JournalEntryType.GENERAL_LEDGER;
        }
        try {
            return JournalEntryType.valueOf(entryType);
        } catch (IllegalArgumentException e) {
            return JournalEntryType.GENERAL_LEDGER;
        }
    }

    /**
     * Resolves a human-readable account name from the account code.
     * In a full ERP this would query a Chart of Accounts table.
     */
    private String resolveAccountName(String accountCode) {
        return switch (accountCode) {
            case "SALARY_EXPENSE"  -> "Salary Expense";
            case "PAYROLL_PAYABLE" -> "Payroll Payable";
            case "CASH"            -> "Cash and Cash Equivalents";
            case "ACCOUNTS_PAY"    -> "Accounts Payable";
            case "FARMER_PAYABLE"  -> "Farmer Payable";
            case "PLATFORM_FEE_REVENUE" -> "Platform Fee Revenue";
            case "DELIVERY_FEE_REVENUE" -> "Delivery Fee Revenue";
            default                -> accountCode;
        };
    }

    private JournalEntryResult buildResult(JournalEntryEntity entry, boolean wasIdempotent, String message) {
        return JournalEntryResult.builder()
                .id(entry.getId())
                .reference(entry.getReference())
                .status(entry.getStatus().name())
                .totalAmount(entry.getTotalAmount())
                .currency(entry.getCurrency())
                .wasIdempotent(wasIdempotent)
                .createdAt(entry.getCreatedAt())
                .message(message)
                .build();
    }
}
