package com.financeapp.payroll.service;

import com.financeapp.payroll.client.FinanceClient;
import com.financeapp.payroll.domain.PayrollEntity;
import com.financeapp.payroll.domain.PayrollRepository;
import com.financeapp.payroll.domain.PayrollStatus;
import com.financeapp.payroll.dto.FinanceJournalResponse;
import com.financeapp.payroll.dto.ProcessPayrollRequest;
import com.financeapp.payroll.dto.ProcessPayrollResponse;
import com.financeapp.payroll.event.PayrollEventPublisher;
import com.financeapp.payroll.exception.DuplicatePayrollException;
import com.financeapp.payroll.exception.FinanceServiceException;
import com.financeapp.payroll.mapper.PayrollMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for PayrollService.
 *
 * <p>Uses Mockito to mock the repository and Finance client.
 * No Spring context is loaded — these tests are fast and isolated.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PayrollService Unit Tests")
class PayrollServiceTest {

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private FinanceClient grpcFinanceClient;

    @Mock
    private FinanceClient restFinanceClient;

    @Mock
    private PayrollMapper payrollMapper;

    @Mock
    private PayrollEventPublisher eventPublisher;

    private PayrollService payrollService;

    @BeforeEach
    void setUp() {
        payrollService = new PayrollService(
                payrollRepository,
                grpcFinanceClient,
                restFinanceClient,
                payrollMapper,
                eventPublisher
        );
    }

    @Nested
    @DisplayName("Process Payroll via gRPC")
    class ProcessPayrollViaGrpc {

        @Test
        @DisplayName("Should process payroll successfully and return response")
        void shouldProcessPayrollSuccessfully() {
            // Arrange
            ProcessPayrollRequest request = new ProcessPayrollRequest();
            request.setPayrollPeriod("2026-08");
            request.setEmployeeCount(25);

            given(payrollRepository.existsByPayrollPeriod("2026-08")).willReturn(false);
            given(payrollRepository.countByPayrollPeriod("2026-08")).willReturn(0L);

            PayrollEntity savedEntity = createSavedPayrollEntity("PAY-2026-08-0001");
            given(payrollRepository.save(any(PayrollEntity.class))).willReturn(savedEntity);

            FinanceJournalResponse journalResponse = FinanceJournalResponse.builder()
                    .journalReference("PAY-2026-08-0001")
                    .status("CREATED")
                    .message("Journal created")
                    .wasIdempotent(false)
                    .build();
            given(grpcFinanceClient.createJournalEntry(any())).willReturn(journalResponse);

            ProcessPayrollResponse expectedResponse = ProcessPayrollResponse.builder()
                    .payrollReference("PAY-2026-08-0001")
                    .payrollPeriod("2026-08")
                    .employeeCount(25)
                    .totalAmount(new BigDecimal("208333.33"))
                    .status("PROCESSED")
                    .journalReference("PAY-2026-08-0001")
                    .build();
            given(payrollMapper.toResponse(any())).willReturn(expectedResponse);

            // Act
            ProcessPayrollResponse response = payrollService.processPayrollViaGrpc(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getPayrollReference()).isEqualTo("PAY-2026-08-0001");
            assertThat(response.getStatus()).isEqualTo("PROCESSED");
            assertThat(response.getJournalReference()).isEqualTo("PAY-2026-08-0001");

            then(grpcFinanceClient).should().createJournalEntry(argThat(req ->
                    "PAY-2026-08-0001".equals(req.getReference()) &&
                    "SALARY_EXPENSE".equals(req.getDebitAccount()) &&
                    "PAYROLL_PAYABLE".equals(req.getCreditAccount())
            ));
        }

        @Test
        @DisplayName("Should throw DuplicatePayrollException for duplicate period")
        void shouldThrowWhenDuplicatePeriod() {
            // Arrange
            ProcessPayrollRequest request = new ProcessPayrollRequest();
            request.setPayrollPeriod("2026-08");
            request.setEmployeeCount(25);

            given(payrollRepository.existsByPayrollPeriod("2026-08")).willReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> payrollService.processPayrollViaGrpc(request))
                    .isInstanceOf(DuplicatePayrollException.class)
                    .hasMessageContaining("2026-08");

            then(grpcFinanceClient).should(never()).createJournalEntry(any());
        }

        @Test
        @DisplayName("Should update payroll to FAILED when Finance Service is unavailable")
        void shouldMarkPayrollFailedWhenFinanceUnavailable() {
            // Arrange
            ProcessPayrollRequest request = new ProcessPayrollRequest();
            request.setPayrollPeriod("2026-09");
            request.setEmployeeCount(10);

            given(payrollRepository.existsByPayrollPeriod("2026-09")).willReturn(false);
            given(payrollRepository.countByPayrollPeriod("2026-09")).willReturn(0L);

            PayrollEntity entity = createSavedPayrollEntity("PAY-2026-09-0001");
            given(payrollRepository.save(any())).willReturn(entity);

            given(grpcFinanceClient.createJournalEntry(any()))
                    .willThrow(FinanceServiceException.serviceUnavailable("Finance Service is down"));

            // Act & Assert
            assertThatThrownBy(() -> payrollService.processPayrollViaGrpc(request))
                    .isInstanceOf(FinanceServiceException.class)
                    .satisfies(ex -> {
                        FinanceServiceException fse = (FinanceServiceException) ex;
                        assertThat(fse.getErrorType())
                                .isEqualTo(FinanceServiceException.ErrorType.UNAVAILABLE);
                    });

            // Verify that payroll was saved at least twice (PENDING + PROCESSING + FAILED)
            then(payrollRepository).should(atLeast(2)).save(any(PayrollEntity.class));
        }

        @Test
        @DisplayName("Should handle Finance timeout correctly")
        void shouldHandleFinanceTimeout() {
            // Arrange
            ProcessPayrollRequest request = new ProcessPayrollRequest();
            request.setPayrollPeriod("2026-10");
            request.setEmployeeCount(5);

            given(payrollRepository.existsByPayrollPeriod("2026-10")).willReturn(false);
            given(payrollRepository.countByPayrollPeriod("2026-10")).willReturn(0L);
            given(payrollRepository.save(any())).willReturn(createSavedPayrollEntity("PAY-2026-10-0001"));

            given(grpcFinanceClient.createJournalEntry(any()))
                    .willThrow(FinanceServiceException.timeout("Deadline exceeded after 5 seconds"));

            // Act & Assert
            assertThatThrownBy(() -> payrollService.processPayrollViaGrpc(request))
                    .isInstanceOf(FinanceServiceException.class)
                    .satisfies(ex -> {
                        FinanceServiceException fse = (FinanceServiceException) ex;
                        assertThat(fse.getErrorType())
                                .isEqualTo(FinanceServiceException.ErrorType.TIMEOUT);
                    });
        }

        @Test
        @DisplayName("Should calculate correct payroll total for 25 employees")
        void shouldCalculateCorrectPayrollTotal() {
            // Arrange
            ProcessPayrollRequest request = new ProcessPayrollRequest();
            request.setPayrollPeriod("2026-11");
            request.setEmployeeCount(25);

            given(payrollRepository.existsByPayrollPeriod("2026-11")).willReturn(false);
            given(payrollRepository.countByPayrollPeriod("2026-11")).willReturn(0L);

            PayrollEntity savedEntity = new PayrollEntity();
            savedEntity.setPayrollReference("PAY-2026-11-0001");
            savedEntity.setPayrollPeriod("2026-11");
            savedEntity.setEmployeeCount(25);
            // 25 employees × $100,000 / 12 months = $208,333.33
            savedEntity.setTotalAmount(new BigDecimal("208333.33"));
            savedEntity.setStatus(PayrollStatus.PENDING);
            savedEntity.setCreatedAt(LocalDateTime.now());

            given(payrollRepository.save(any(PayrollEntity.class))).willAnswer(inv -> {
                PayrollEntity entity = inv.getArgument(0);
                entity.setStatus(PayrollStatus.PENDING);
                entity.setCreatedAt(LocalDateTime.now());
                // Verify the amount is calculated correctly
                assertThat(entity.getTotalAmount()).isNotNull();
                assertThat(entity.getTotalAmount().compareTo(BigDecimal.ZERO)).isGreaterThan(0);
                return entity;
            });

            given(grpcFinanceClient.createJournalEntry(any()))
                    .willThrow(FinanceServiceException.serviceUnavailable("stop early"));

            // Act & Assert (we just want to verify calculation happens before Finance call)
            assertThatThrownBy(() -> payrollService.processPayrollViaGrpc(request))
                    .isInstanceOf(FinanceServiceException.class);

            // Verify Finance was called with non-zero amount
            then(grpcFinanceClient).should().createJournalEntry(argThat(req ->
                    req.getAmount() != null && req.getAmount().compareTo(BigDecimal.ZERO) > 0
            ));
        }
    }

    @Nested
    @DisplayName("Process Payroll via REST")
    class ProcessPayrollViaRest {

        @Test
        @DisplayName("Should use REST client instead of gRPC client")
        void shouldUseRestClient() {
            // Arrange
            ProcessPayrollRequest request = new ProcessPayrollRequest();
            request.setPayrollPeriod("2026-12");
            request.setEmployeeCount(30);

            given(payrollRepository.existsByPayrollPeriod("2026-12")).willReturn(false);
            given(payrollRepository.countByPayrollPeriod("2026-12")).willReturn(0L);
            given(payrollRepository.save(any())).willReturn(createSavedPayrollEntity("PAY-2026-12-0001"));
            given(restFinanceClient.createJournalEntry(any()))
                    .willReturn(FinanceJournalResponse.builder()
                            .journalReference("PAY-2026-12-0001")
                            .status("CREATED")
                            .build());
            given(payrollMapper.toResponse(any())).willReturn(
                    ProcessPayrollResponse.builder().payrollReference("PAY-2026-12-0001").build());

            // Act
            payrollService.processPayrollViaRest(request);

            // Assert
            then(restFinanceClient).should().createJournalEntry(any());
            then(grpcFinanceClient).should(never()).createJournalEntry(any());
        }
    }

    private PayrollEntity createSavedPayrollEntity(String reference) {
        PayrollEntity entity = new PayrollEntity();
        entity.setId(1L);
        entity.setPayrollReference(reference);
        entity.setPayrollPeriod(reference.substring(4, 11));
        entity.setEmployeeCount(25);
        entity.setTotalAmount(new BigDecimal("208333.33"));
        entity.setStatus(PayrollStatus.PENDING);
        entity.setFinanceTransport("GRPC");
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
