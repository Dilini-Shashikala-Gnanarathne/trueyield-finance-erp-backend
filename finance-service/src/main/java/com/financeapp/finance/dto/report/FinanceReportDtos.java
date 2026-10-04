package com.financeapp.finance.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTOs for the finance read APIs (journal listing, ledger, trial balance, statements).
 */
public final class FinanceReportDtos {

    private FinanceReportDtos() {
    }

    public record LineDto(String lineType, String accountCode, String accountName,
                          BigDecimal amount, String description) {
    }

    public record JournalEntryDto(Long id, String reference, String description, String entryType,
                                  String currency, BigDecimal totalAmount, String status,
                                  String sourceSystem, String orderId, String farmerId, String buyerId,
                                  LocalDateTime createdAt, List<LineDto> lines) {
    }

    public record PagedResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    }

    public record LedgerLineDto(String reference, LocalDateTime date, String lineType,
                                BigDecimal amount, String description) {
    }

    public record LedgerDto(String accountCode, String accountName, BigDecimal totalDebit,
                            BigDecimal totalCredit, BigDecimal balance, PagedResult<LedgerLineDto> lines) {
    }

    public record TrialBalanceRow(String accountCode, String accountName,
                                  BigDecimal totalDebit, BigDecimal totalCredit) {
    }

    public record TrialBalanceDto(List<TrialBalanceRow> accounts, BigDecimal totalDebit,
                                  BigDecimal totalCredit, boolean balanced) {
    }

    public record StatementItemDto(String orderId, String reference, LocalDateTime date, String status,
                                   BigDecimal amount, BigDecimal platformFee, BigDecimal deliveryFee) {
    }

    /**
     * Party statement. For farmers {@code amount} is the payable earned (net of platform fee);
     * for buyers it is the total amount paid. Reversed entries are listed but excluded from the total.
     */
    public record StatementDto(String partyId, String partyType, String currency, BigDecimal total,
                               PagedResult<StatementItemDto> items) {
    }
}
