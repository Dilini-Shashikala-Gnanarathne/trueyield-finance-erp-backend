package com.financeapp.finance.service;

import com.financeapp.finance.domain.JournalEntryEntity;
import com.financeapp.finance.domain.JournalEntryLineEntity;
import com.financeapp.finance.domain.JournalEntryStatus;
import com.financeapp.finance.dto.CreateJournalEntryCommand;
import com.financeapp.finance.dto.JournalEntryResult;
import com.financeapp.finance.repository.JournalEntryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for JournalEntryApplicationService.
 *
 * <p>Tests all the core business scenarios:
 * - Successful journal creation
 * - Idempotent return for existing reference
 * - Validation failures
 * - Double-entry structure verification
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JournalEntryApplicationService Unit Tests")
class JournalEntryApplicationServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @InjectMocks
    private JournalEntryApplicationService journalEntryApplicationService;

    @Nested
    @DisplayName("Create Journal Entry")
    class CreateJournalEntry {

        @Test
        @DisplayName("Should create journal entry with debit and credit lines")
        void shouldCreateJournalEntryWithDoubleEntryLines() {
            // Arrange
            CreateJournalEntryCommand command = validCommand("PAY-2026-08-0001");

            given(journalEntryRepository.findByReference("PAY-2026-08-0001"))
                    .willReturn(Optional.empty());

            JournalEntryEntity savedEntity = createSavedEntity("PAY-2026-08-0001");
            given(journalEntryRepository.save(any(JournalEntryEntity.class)))
                    .willReturn(savedEntity);

            // Act
            JournalEntryResult result = journalEntryApplicationService.createJournalEntry(command);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getReference()).isEqualTo("PAY-2026-08-0001");
            assertThat(result.getStatus()).isEqualTo("CREATED");
            assertThat(result.isWasIdempotent()).isFalse();

            // Verify the saved entity has 2 lines (debit + credit)
            ArgumentCaptor<JournalEntryEntity> entityCaptor =
                    ArgumentCaptor.forClass(JournalEntryEntity.class);
            then(journalEntryRepository).should().save(entityCaptor.capture());

            JournalEntryEntity capturedEntity = entityCaptor.getValue();
            assertThat(capturedEntity.getLines()).hasSize(2);
            assertThat(capturedEntity.getLines())
                    .extracting(JournalEntryLineEntity::getLineType)
                    .containsExactlyInAnyOrder(
                            JournalEntryLineEntity.LineType.DEBIT,
                            JournalEntryLineEntity.LineType.CREDIT
                    );
            assertThat(capturedEntity.getLines())
                    .extracting(JournalEntryLineEntity::getAccountCode)
                    .containsExactlyInAnyOrder("SALARY_EXPENSE", "PAYROLL_PAYABLE");
        }

        @Test
        @DisplayName("Should return existing journal for duplicate reference (idempotent)")
        void shouldReturnExistingJournalForDuplicateReference() {
            // Arrange
            CreateJournalEntryCommand command = validCommand("PAY-2026-08-0001");

            JournalEntryEntity existingEntity = createSavedEntity("PAY-2026-08-0001");
            given(journalEntryRepository.findByReference("PAY-2026-08-0001"))
                    .willReturn(Optional.of(existingEntity));

            // Act
            JournalEntryResult result = journalEntryApplicationService.createJournalEntry(command);

            // Assert
            assertThat(result.isWasIdempotent()).isTrue();
            assertThat(result.getReference()).isEqualTo("PAY-2026-08-0001");

            // No new entity should be saved
            then(journalEntryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Should reject negative amount")
        void shouldRejectNegativeAmount() {
            // Arrange
            given(journalEntryRepository.findByReference(anyString())).willReturn(Optional.empty());

            CreateJournalEntryCommand command = CreateJournalEntryCommand.builder()
                    .reference("PAY-2026-08-0001")
                    .description("Test")
                    .debitAccount("SALARY_EXPENSE")
                    .creditAccount("PAYROLL_PAYABLE")
                    .amount(new BigDecimal("-100.00"))
                    .currency("USD")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> journalEntryApplicationService.createJournalEntry(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive");
        }

        @Test
        @DisplayName("Should reject when debit and credit accounts are the same")
        void shouldRejectWhenSameDebitAndCreditAccount() {
            // Arrange
            given(journalEntryRepository.findByReference(anyString())).willReturn(Optional.empty());

            CreateJournalEntryCommand command = CreateJournalEntryCommand.builder()
                    .reference("PAY-2026-08-0001")
                    .description("Test")
                    .debitAccount("SALARY_EXPENSE")
                    .creditAccount("SALARY_EXPENSE") // Same as debit
                    .amount(new BigDecimal("1000.00"))
                    .currency("USD")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> journalEntryApplicationService.createJournalEntry(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be the same");
        }

        @Test
        @DisplayName("Should reject invalid currency code")
        void shouldRejectInvalidCurrencyCode() {
            // Arrange
            given(journalEntryRepository.findByReference(anyString())).willReturn(Optional.empty());

            CreateJournalEntryCommand command = CreateJournalEntryCommand.builder()
                    .reference("PAY-2026-08-0001")
                    .description("Test")
                    .debitAccount("SALARY_EXPENSE")
                    .creditAccount("PAYROLL_PAYABLE")
                    .amount(new BigDecimal("1000.00"))
                    .currency("DOLLARS") // Too long, not ISO 4217
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> journalEntryApplicationService.createJournalEntry(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ISO 4217");
        }

        @Test
        @DisplayName("Should set debit line amount equal to credit line amount")
        void shouldHaveBalancedDebitAndCreditLines() {
            // Arrange
            BigDecimal payrollAmount = new BigDecimal("208333.33");
            CreateJournalEntryCommand command = CreateJournalEntryCommand.builder()
                    .reference("PAY-2026-08-0001")
                    .description("Payroll test")
                    .debitAccount("SALARY_EXPENSE")
                    .creditAccount("PAYROLL_PAYABLE")
                    .amount(payrollAmount)
                    .currency("USD")
                    .sourceSystem("PAYROLL-SERVICE")
                    .build();

            given(journalEntryRepository.findByReference(anyString())).willReturn(Optional.empty());
            given(journalEntryRepository.save(any())).willReturn(createSavedEntity("PAY-2026-08-0001"));

            // Act
            journalEntryApplicationService.createJournalEntry(command);

            // Assert: debit amount = credit amount (double-entry accounting balance)
            ArgumentCaptor<JournalEntryEntity> captor = ArgumentCaptor.forClass(JournalEntryEntity.class);
            then(journalEntryRepository).should().save(captor.capture());

            JournalEntryEntity entity = captor.getValue();
            BigDecimal totalDebits = entity.getLines().stream()
                    .filter(l -> l.getLineType() == JournalEntryLineEntity.LineType.DEBIT)
                    .map(JournalEntryLineEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCredits = entity.getLines().stream()
                    .filter(l -> l.getLineType() == JournalEntryLineEntity.LineType.CREDIT)
                    .map(JournalEntryLineEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            assertThat(totalDebits).isEqualByComparingTo(totalCredits);
            assertThat(totalDebits).isEqualByComparingTo(payrollAmount);
        }
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

    private JournalEntryEntity createSavedEntity(String reference) {
        JournalEntryEntity entity = new JournalEntryEntity();
        entity.setId(1L);
        entity.setReference(reference);
        entity.setDescription("Test payroll journal");
        entity.setStatus(JournalEntryStatus.CREATED);
        entity.setTotalAmount(new BigDecimal("208333.33"));
        entity.setCurrency("USD");
        entity.setSourceSystem("PAYROLL-SERVICE");
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
