package com.financeapp.finance.service;

import com.financeapp.finance.domain.JournalEntryEntity;
import com.financeapp.finance.domain.JournalEntryLineEntity;
import com.financeapp.finance.domain.JournalEntryLineEntity.LineType;
import com.financeapp.finance.domain.JournalEntryStatus;
import com.financeapp.finance.dto.report.FinanceReportDtos.*;
import com.financeapp.finance.repository.JournalEntryLineRepository;
import com.financeapp.finance.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Read side of the finance ledger (journal listing, account ledger, trial balance,
 * farmer/buyer statements) plus journal reversal.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceReportService {

    private static final String REVERSAL_PREFIX = "REV-";

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository lineRepository;
    private final FinancialAuditService financialAuditService;

    // ------------------------------------------------------------------
    // Journal entries
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PagedResult<JournalEntryDto> listJournalEntries(JournalEntryStatus status, int page, int size) {
        Pageable pageable = pageable(page, size, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        Page<JournalEntryEntity> result = status != null
                ? journalEntryRepository.findByStatus(status, pageable)
                : journalEntryRepository.findAll(pageable);
        return toPaged(result.map(this::toDto));
    }

    @Transactional(readOnly = true)
    public JournalEntryDto getJournalEntry(String reference) {
        return toDto(findOrThrow(reference));
    }

    /**
     * Reverses a journal entry by posting a mirror entry (debits and credits swapped)
     * and marking the original REVERSED. The ledger is never edited in place.
     */
    @Transactional
    public JournalEntryDto reverseJournalEntry(String reference, String reason, String performedBy) {
        if (reference.startsWith(REVERSAL_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A reversal entry cannot itself be reversed.");
        }
        JournalEntryEntity original = findOrThrow(reference);
        if (original.getStatus() == JournalEntryStatus.REVERSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Journal entry is already reversed: " + reference);
        }
        String reversalReference = REVERSAL_PREFIX + reference;
        if (journalEntryRepository.existsByReference(reversalReference)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reversal already exists for: " + reference);
        }

        String suffix = (reason != null && !reason.isBlank()) ? " - " + reason.trim() : "";
        String description = ("Reversal of " + reference + suffix);

        JournalEntryEntity reversal = new JournalEntryEntity();
        reversal.setReference(reversalReference);
        reversal.setDescription(description.length() > 500 ? description.substring(0, 500) : description);
        reversal.setEntryType(original.getEntryType());
        reversal.setCurrency(original.getCurrency());
        reversal.setTotalAmount(original.getTotalAmount());
        reversal.setStatus(JournalEntryStatus.CREATED);
        reversal.setSourceSystem("finance-service");
        reversal.setOrderId(original.getOrderId());

        for (JournalEntryLineEntity line : original.getLines()) {
            JournalEntryLineEntity mirror = new JournalEntryLineEntity();
            mirror.setLineType(line.getLineType() == LineType.DEBIT ? LineType.CREDIT : LineType.DEBIT);
            mirror.setAccountCode(line.getAccountCode());
            mirror.setAccountName(line.getAccountName());
            mirror.setAmount(line.getAmount());
            mirror.setDescription("Reversal: " + (line.getDescription() != null ? line.getDescription() : reference));
            reversal.addLine(mirror);
        }

        original.setStatus(JournalEntryStatus.REVERSED);
        journalEntryRepository.save(original);
        JournalEntryEntity saved = journalEntryRepository.save(reversal);

        financialAuditService.recordEntryAudit(original, "REVERSED", performedBy);
        financialAuditService.recordEntryAudit(saved, "CREATED", performedBy);

        log.info("Journal reversed. original={}, reversal={}, by={}", reference, reversalReference, performedBy);
        return toDto(saved);
    }

    // ------------------------------------------------------------------
    // Ledger & trial balance
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public TrialBalanceDto trialBalance() {
        Map<String, BigDecimal[]> totals = new LinkedHashMap<>();
        Map<String, String> names = new LinkedHashMap<>();
        for (Object[] row : lineRepository.sumByAccountAndSide()) {
            accumulate(totals, names, row);
        }

        List<TrialBalanceRow> rows = new ArrayList<>();
        BigDecimal debit = BigDecimal.ZERO;
        BigDecimal credit = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal[]> e : totals.entrySet()) {
            rows.add(new TrialBalanceRow(e.getKey(), names.get(e.getKey()), e.getValue()[0], e.getValue()[1]));
            debit = debit.add(e.getValue()[0]);
            credit = credit.add(e.getValue()[1]);
        }
        return new TrialBalanceDto(rows, debit, credit, debit.compareTo(credit) == 0);
    }

    @Transactional(readOnly = true)
    public LedgerDto ledger(String accountCode, int page, int size) {
        Map<String, BigDecimal[]> totals = new LinkedHashMap<>();
        Map<String, String> names = new LinkedHashMap<>();
        for (Object[] row : lineRepository.sumForAccount(accountCode)) {
            accumulate(totals, names, row);
        }
        if (!totals.containsKey(accountCode)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No ledger activity for account: " + accountCode);
        }
        BigDecimal[] sums = totals.get(accountCode);

        Page<LedgerLineDto> lines = lineRepository
                .findByAccountCodeOrderByIdDesc(accountCode, pageable(page, size, org.springframework.data.domain.Sort.unsorted()))
                .map(l -> new LedgerLineDto(
                        l.getJournalEntry().getReference(),
                        l.getJournalEntry().getCreatedAt(),
                        l.getLineType().name(),
                        l.getAmount(),
                        l.getDescription()));

        return new LedgerDto(accountCode, names.get(accountCode), sums[0], sums[1],
                sums[0].subtract(sums[1]), toPaged(lines));
    }

    // ------------------------------------------------------------------
    // Statements
    // ------------------------------------------------------------------

    /** Farmer statement: payable earned per paid order (net of platform fee). */
    @Transactional(readOnly = true)
    public StatementDto farmerStatement(String farmerId, int page, int size) {
        Page<JournalEntryEntity> entries = journalEntryRepository
                .findByFarmerIdOrderByCreatedAtDesc(farmerId, pageable(page, size, org.springframework.data.domain.Sort.unsorted()));
        return buildStatement(farmerId, "FARMER", entries, e -> sumLines(e, "FARMER_PAYABLE", LineType.CREDIT));
    }

    /** Buyer statement: total paid per order. */
    @Transactional(readOnly = true)
    public StatementDto buyerStatement(String buyerId, int page, int size) {
        Page<JournalEntryEntity> entries = journalEntryRepository
                .findByBuyerIdOrderByCreatedAtDesc(buyerId, pageable(page, size, org.springframework.data.domain.Sort.unsorted()));
        return buildStatement(buyerId, "BUYER", entries, JournalEntryEntity::getTotalAmount);
    }

    private StatementDto buildStatement(String partyId, String partyType, Page<JournalEntryEntity> entries,
                                        Function<JournalEntryEntity, BigDecimal> amountFn) {
        BigDecimal total = BigDecimal.ZERO;
        String currency = "LKR";
        List<StatementItemDto> items = new ArrayList<>();
        for (JournalEntryEntity e : entries.getContent()) {
            BigDecimal amount = amountFn.apply(e);
            currency = e.getCurrency();
            if (e.getStatus() != JournalEntryStatus.REVERSED) {
                total = total.add(amount);
            }
            items.add(new StatementItemDto(
                    e.getOrderId(), e.getReference(), e.getCreatedAt(), e.getStatus().name(), amount,
                    sumLines(e, "PLATFORM_FEE_REVENUE", LineType.CREDIT),
                    sumLines(e, "DELIVERY_FEE_REVENUE", LineType.CREDIT)));
        }
        PagedResult<StatementItemDto> paged = new PagedResult<>(items, entries.getNumber(), entries.getSize(),
                entries.getTotalElements(), entries.getTotalPages());
        return new StatementDto(partyId, partyType, currency, total, paged);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private JournalEntryEntity findOrThrow(String reference) {
        return journalEntryRepository.findByReference(reference)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Journal entry not found: " + reference));
    }

    private BigDecimal sumLines(JournalEntryEntity entry, String accountCode, LineType type) {
        return entry.getLines().stream()
                .filter(l -> l.getLineType() == type && accountCode.equals(l.getAccountCode()))
                .map(JournalEntryLineEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void accumulate(Map<String, BigDecimal[]> totals, Map<String, String> names, Object[] row) {
        String code = (String) row[0];
        names.putIfAbsent(code, (String) row[1]);
        LineType type = (LineType) row[2];
        BigDecimal sum = (BigDecimal) row[3];
        BigDecimal[] pair = totals.computeIfAbsent(code, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        if (type == LineType.DEBIT) {
            pair[0] = pair[0].add(sum);
        } else {
            pair[1] = pair[1].add(sum);
        }
    }

    private Pageable pageable(int page, int size, org.springframework.data.domain.Sort sort) {
        return PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), sort);
    }

    private <T> PagedResult<T> toPaged(Page<T> page) {
        return new PagedResult<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    private JournalEntryDto toDto(JournalEntryEntity e) {
        List<LineDto> lines = e.getLines().stream()
                .map(l -> new LineDto(l.getLineType().name(), l.getAccountCode(), l.getAccountName(),
                        l.getAmount(), l.getDescription()))
                .toList();
        return new JournalEntryDto(e.getId(), e.getReference(), e.getDescription(), e.getEntryType().name(),
                e.getCurrency(), e.getTotalAmount(), e.getStatus().name(), e.getSourceSystem(),
                e.getOrderId(), e.getFarmerId(), e.getBuyerId(), e.getCreatedAt(), lines);
    }
}
