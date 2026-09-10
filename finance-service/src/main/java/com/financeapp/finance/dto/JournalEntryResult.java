package com.financeapp.finance.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transport-agnostic result DTO for journal entry creation.
 * Used by both gRPC and REST paths in Finance Service.
 */
@Data
@Builder
public class JournalEntryResult {

    private Long id;
    private String reference;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private boolean wasIdempotent;
    private LocalDateTime createdAt;
    private String message;
}
