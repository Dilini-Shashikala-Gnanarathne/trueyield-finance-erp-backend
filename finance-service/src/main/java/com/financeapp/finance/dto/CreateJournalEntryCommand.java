package com.financeapp.finance.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Transport-agnostic input DTO for journal entry creation.
 * Used by both gRPC and REST paths in Finance Service.
 */
@Data
@Builder
public class CreateJournalEntryCommand {

    private String reference;
    private String description;
    private String debitAccount;
    private String creditAccount;
    private BigDecimal amount;
    private String currency;
    private String sourceSystem;
    private String entryType;
}
