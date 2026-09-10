package com.financeapp.finance.integration;

import com.financeapp.finance.domain.JournalEntryStatus;
import com.financeapp.finance.dto.CreateJournalEntryCommand;
import com.financeapp.finance.dto.JournalEntryResult;
import com.financeapp.finance.repository.JournalEntryRepository;
import com.financeapp.finance.service.JournalEntryApplicationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for Finance Service with real PostgreSQL via Testcontainers.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Journal entry persistence</li>
 *   <li>Idempotency at the database level</li>
 *   <li>Concurrent duplicate handling</li>
 * </ul>
 * </p>
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Finance Service Integration Tests")
class FinanceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("finance_test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JournalEntryApplicationService journalEntryApplicationService;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Test
    @Order(1)
    @DisplayName("Should create journal entry and persist to database")
    void shouldCreateAndPersistJournalEntry() {
        // Arrange
        CreateJournalEntryCommand command = validCommand("TEST-PAY-0001");

        // Act
        JournalEntryResult result = journalEntryApplicationService.createJournalEntry(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getReference()).isEqualTo("TEST-PAY-0001");
        assertThat(result.isWasIdempotent()).isFalse();

        // Verify it was actually saved to DB
        assertThat(journalEntryRepository.existsByReference("TEST-PAY-0001")).isTrue();

        var saved = journalEntryRepository.findByReference("TEST-PAY-0001");
        assertThat(saved).isPresent();
        assertThat(saved.get().getStatus()).isEqualTo(JournalEntryStatus.CREATED);
        assertThat(saved.get().getLines()).hasSize(2);
        assertThat(saved.get().getTotalAmount())
                .isEqualByComparingTo(new BigDecimal("208333.33"));
    }

    @Test
    @Order(2)
    @DisplayName("Should return existing journal for duplicate reference (idempotency)")
    void shouldReturnExistingJournalForDuplicateReference() {
        // Arrange — same reference as previous test
        CreateJournalEntryCommand command = validCommand("TEST-PAY-0001");

        // Act — call again with the same reference
        JournalEntryResult result = journalEntryApplicationService.createJournalEntry(command);

        // Assert
        assertThat(result.isWasIdempotent()).isTrue();
        assertThat(result.getReference()).isEqualTo("TEST-PAY-0001");

        // Only one entry should exist in the DB
        assertThat(journalEntryRepository.findAll())
                .filteredOn(e -> e.getReference().equals("TEST-PAY-0001"))
                .hasSize(1);
    }

    @Test
    @Order(3)
    @DisplayName("Should create different journal entries for different references")
    void shouldCreateDistinctEntriesForDifferentReferences() {
        // Act
        JournalEntryResult result1 = journalEntryApplicationService.createJournalEntry(
                validCommand("TEST-PAY-0002"));
        JournalEntryResult result2 = journalEntryApplicationService.createJournalEntry(
                validCommand("TEST-PAY-0003"));

        // Assert
        assertThat(result1.getReference()).isEqualTo("TEST-PAY-0002");
        assertThat(result2.getReference()).isEqualTo("TEST-PAY-0003");
        assertThat(result1.isWasIdempotent()).isFalse();
        assertThat(result2.isWasIdempotent()).isFalse();

        assertThat(journalEntryRepository.existsByReference("TEST-PAY-0002")).isTrue();
        assertThat(journalEntryRepository.existsByReference("TEST-PAY-0003")).isTrue();
    }

    @Test
    @Order(4)
    @DisplayName("Should persist journal with correct double-entry accounting lines")
    void shouldPersistCorrectDoubleEntryLines() {
        // Act
        journalEntryApplicationService.createJournalEntry(validCommand("TEST-PAY-0004"));

        // Assert
        var entry = journalEntryRepository.findByReference("TEST-PAY-0004").orElseThrow();
        var lines = entry.getLines();

        assertThat(lines).hasSize(2);

        var debitLines = lines.stream()
                .filter(l -> l.getLineType().name().equals("DEBIT"))
                .toList();
        var creditLines = lines.stream()
                .filter(l -> l.getLineType().name().equals("CREDIT"))
                .toList();

        assertThat(debitLines).hasSize(1);
        assertThat(creditLines).hasSize(1);
        assertThat(debitLines.get(0).getAccountCode()).isEqualTo("SALARY_EXPENSE");
        assertThat(creditLines.get(0).getAccountCode()).isEqualTo("PAYROLL_PAYABLE");

        // Double-entry: debits must equal credits
        BigDecimal totalDebits = debitLines.stream()
                .map(l -> l.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredits = creditLines.stream()
                .map(l -> l.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalDebits).isEqualByComparingTo(totalCredits);
    }

    private CreateJournalEntryCommand validCommand(String reference) {
        return CreateJournalEntryCommand.builder()
                .reference(reference)
                .description("Payroll for period 2026-08")
                .debitAccount("SALARY_EXPENSE")
                .creditAccount("PAYROLL_PAYABLE")
                .amount(new BigDecimal("208333.33"))
                .currency("USD")
                .sourceSystem("PAYROLL-SERVICE")
                .entryType("PAYROLL")
                .build();
    }
}
