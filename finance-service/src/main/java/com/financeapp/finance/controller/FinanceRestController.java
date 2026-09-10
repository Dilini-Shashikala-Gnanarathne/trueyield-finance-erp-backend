package com.financeapp.finance.controller;

import com.financeapp.finance.dto.CreateJournalEntryCommand;
import com.financeapp.finance.dto.JournalEntryResult;
import com.financeapp.finance.service.JournalEntryApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * REST controller for Finance Service journal entry operations.
 *
 * <p>This controller exists <strong>primarily for benchmark comparison</strong>.
 * It exposes the same journal entry creation operation as the gRPC server,
 * but over REST/HTTP+JSON. This allows measuring the performance difference
 * between gRPC and REST for the same business operation.</p>
 *
 * <p>In production, internal Finance Service operations would only be accessible
 * via gRPC (not REST). The REST endpoint here is intentionally exposed to allow
 * the Payroll Service's {@link com.financeapp.payroll.client.RestFinanceClient}
 * to call it for benchmark purposes.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceRestController {

    private final JournalEntryApplicationService journalEntryApplicationService;

    /**
     * Create a journal entry via REST.
     * Mirrors the gRPC CreateJournalEntry operation exactly.
     */
    @PostMapping("/journal-entries")
    public ResponseEntity<Map<String, Object>> createJournalEntry(
            @Valid @RequestBody CreateJournalEntryRestRequest request) {

        log.info("REST CreateJournalEntry called. reference={}, amount={}",
                request.getReference(), request.getAmount());

        CreateJournalEntryCommand command = CreateJournalEntryCommand.builder()
                .reference(request.getReference())
                .description(request.getDescription())
                .debitAccount(request.getDebitAccount())
                .creditAccount(request.getCreditAccount())
                .amount(new BigDecimal(request.getAmount()))
                .currency(request.getCurrency())
                .sourceSystem(request.getSourceSystem())
                .build();

        JournalEntryResult result = journalEntryApplicationService.createJournalEntry(command);

        log.info("REST CreateJournalEntry completed. reference={}, wasIdempotent={}",
                result.getReference(), result.isWasIdempotent());

        Map<String, Object> response = Map.of(
                "journalReference", result.getReference(),
                "status", result.getStatus(),
                "message", result.getMessage(),
                "wasIdempotent", result.isWasIdempotent(),
                "createdAt", result.getCreatedAt() != null ? result.getCreatedAt().toString() : ""
        );

        HttpStatus status = result.isWasIdempotent() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "finance-service",
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Inner request DTO for the REST endpoint.
     * Mirrors the protobuf CreateJournalEntryRequest fields.
     */
    @Data
    public static class CreateJournalEntryRestRequest {

        @NotBlank(message = "reference is required")
        private String reference;

        @NotBlank(message = "description is required")
        private String description;

        @NotBlank(message = "debitAccount is required")
        private String debitAccount;

        @NotBlank(message = "creditAccount is required")
        private String creditAccount;

        @NotBlank(message = "amount is required")
        private String amount;

        @NotBlank(message = "currency is required")
        @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO 4217 code")
        private String currency;

        private String sourceSystem;
    }
}
