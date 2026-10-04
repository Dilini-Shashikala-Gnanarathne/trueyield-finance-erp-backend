package com.financeapp.finance.controller;

import com.financeapp.finance.domain.JournalEntryStatus;
import com.financeapp.finance.dto.report.FinanceReportDtos.*;
import com.financeapp.finance.security.SecurityPrincipal;
import com.financeapp.finance.service.FinanceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Read APIs for the finance ledger. Admins see everything; farmers and buyers
 * can only see their own statements.
 */
@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceReportController {

    private final FinanceReportService reportService;

    @GetMapping("/journal-entries")
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResult<JournalEntryDto> listJournalEntries(
            @RequestParam(required = false) JournalEntryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return reportService.listJournalEntries(status, page, size);
    }

    @GetMapping("/journal-entries/{reference}")
    @PreAuthorize("hasRole('ADMIN')")
    public JournalEntryDto getJournalEntry(@PathVariable String reference) {
        return reportService.getJournalEntry(reference);
    }

    @PostMapping("/journal-entries/{reference}/reverse")
    @PreAuthorize("hasRole('ADMIN')")
    public JournalEntryDto reverseJournalEntry(
            @PathVariable String reference,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        String reason = body != null ? body.get("reason") : null;
        return reportService.reverseJournalEntry(reference, reason, principal.getUserId());
    }

    @GetMapping("/trial-balance")
    @PreAuthorize("hasRole('ADMIN')")
    public TrialBalanceDto trialBalance() {
        return reportService.trialBalance();
    }

    @GetMapping("/ledger/{accountCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public LedgerDto ledger(@PathVariable String accountCode,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size) {
        return reportService.ledger(accountCode, page, size);
    }

    @GetMapping("/statements/farmer/me")
    @PreAuthorize("hasRole('FARMER')")
    public StatementDto myFarmerStatement(@AuthenticationPrincipal SecurityPrincipal principal,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return reportService.farmerStatement(principal.getUserId(), page, size);
    }

    @GetMapping("/statements/buyer/me")
    @PreAuthorize("hasRole('BUYER')")
    public StatementDto myBuyerStatement(@AuthenticationPrincipal SecurityPrincipal principal,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return reportService.buyerStatement(principal.getUserId(), page, size);
    }

    @GetMapping("/statements/farmer/{farmerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public StatementDto farmerStatement(@PathVariable String farmerId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return reportService.farmerStatement(farmerId, page, size);
    }

    @GetMapping("/statements/buyer/{buyerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public StatementDto buyerStatement(@PathVariable String buyerId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return reportService.buyerStatement(buyerId, page, size);
    }
}
