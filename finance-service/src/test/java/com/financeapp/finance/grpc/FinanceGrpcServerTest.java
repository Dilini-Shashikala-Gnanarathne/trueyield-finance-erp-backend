package com.financeapp.finance.grpc;

import com.financeapp.finance.dto.JournalEntryResult;
import com.financeapp.finance.grpc.proto.*;
import com.financeapp.finance.service.JournalEntryApplicationService;
import io.grpc.StatusRuntimeException;
import io.grpc.testing.GrpcServerRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static io.grpc.Status.Code.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for the gRPC server adapter (FinanceGrpcServer).
 *
 * <p>These tests use {@link GrpcServerRule} to start an in-process gRPC server
 * that runs in the same JVM — no Docker or network required.
 * The JournalEntryApplicationService is mocked so tests focus on:
 * <ul>
 *   <li>Proto message validation</li>
 *   <li>Correct gRPC status codes for different failure scenarios</li>
 *   <li>Response proto construction</li>
 *   <li>Idempotent response handling</li>
 * </ul>
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FinanceGrpcServer Unit Tests")
class FinanceGrpcServerTest {

    @Mock
    private JournalEntryApplicationService journalEntryApplicationService;

    private FinanceServiceGrpc.FinanceServiceBlockingStub blockingStub;

    @BeforeEach
    void setUp() throws Exception {
        // In-process gRPC server for testing
        io.grpc.Server server = io.grpc.ServerBuilder
                .forPort(0) // Random port
                .addService(new FinanceGrpcServer(journalEntryApplicationService))
                .build()
                .start();

        io.grpc.ManagedChannel channel = io.grpc.ManagedChannelBuilder
                .forAddress("localhost", server.getPort())
                .usePlaintext()
                .build();

        blockingStub = FinanceServiceGrpc.newBlockingStub(channel);
    }

    @Nested
    @DisplayName("CreateJournalEntry RPC")
    class CreateJournalEntryRpc {

        @Test
        @DisplayName("Should return CREATED response for valid request")
        void shouldSucceedWithValidRequest() {
            // Arrange
            given(journalEntryApplicationService.createJournalEntry(any()))
                    .willReturn(JournalEntryResult.builder()
                            .reference("PAY-2026-08-0001")
                            .status("CREATED")
                            .wasIdempotent(false)
                            .message("Journal created")
                            .createdAt(LocalDateTime.now())
                            .totalAmount(new BigDecimal("208333.33"))
                            .build());

            CreateJournalEntryRequest request = validRequest("PAY-2026-08-0001");

            // Act
            CreateJournalEntryResponse response = blockingStub.createJournalEntry(request);

            // Assert
            assertThat(response.getJournalReference()).isEqualTo("PAY-2026-08-0001");
            assertThat(response.getStatus()).isEqualTo(JournalStatus.CREATED);
            assertThat(response.getWasIdempotent()).isFalse();
        }

        @Test
        @DisplayName("Should return wasIdempotent=true for duplicate reference")
        void shouldReturnIdempotentResponseForDuplicateReference() {
            // Arrange
            given(journalEntryApplicationService.createJournalEntry(any()))
                    .willReturn(JournalEntryResult.builder()
                            .reference("PAY-2026-08-0001")
                            .status("CREATED")
                            .wasIdempotent(true) // Already existed
                            .message("Idempotent return")
                            .createdAt(LocalDateTime.now().minusHours(1))
                            .build());

            CreateJournalEntryRequest request = validRequest("PAY-2026-08-0001");

            // Act
            CreateJournalEntryResponse response = blockingStub.createJournalEntry(request);

            // Assert
            assertThat(response.getWasIdempotent()).isTrue();
            assertThat(response.getJournalReference()).isEqualTo("PAY-2026-08-0001");
        }

        @Test
        @DisplayName("Should return INVALID_ARGUMENT for missing reference")
        void shouldReturnInvalidArgumentWhenReferenceIsMissing() {
            // Arrange
            CreateJournalEntryRequest request = CreateJournalEntryRequest.newBuilder()
                    .setReference("") // Empty reference
                    .setDebitAccount("SALARY_EXPENSE")
                    .setCreditAccount("PAYROLL_PAYABLE")
                    .setAmount("1000.00")
                    .setCurrency("USD")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> blockingStub.createJournalEntry(request))
                    .isInstanceOf(StatusRuntimeException.class)
                    .satisfies(ex -> {
                        StatusRuntimeException sre = (StatusRuntimeException) ex;
                        assertThat(sre.getStatus().getCode()).isEqualTo(INVALID_ARGUMENT);
                        assertThat(sre.getStatus().getDescription()).contains("reference is required");
                    });
        }

        @Test
        @DisplayName("Should return INVALID_ARGUMENT for invalid amount")
        void shouldReturnInvalidArgumentForNonNumericAmount() {
            // Arrange
            CreateJournalEntryRequest request = CreateJournalEntryRequest.newBuilder()
                    .setReference("PAY-2026-08-0001")
                    .setDebitAccount("SALARY_EXPENSE")
                    .setCreditAccount("PAYROLL_PAYABLE")
                    .setAmount("not-a-number")
                    .setCurrency("USD")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> blockingStub.createJournalEntry(request))
                    .isInstanceOf(StatusRuntimeException.class)
                    .satisfies(ex -> {
                        StatusRuntimeException sre = (StatusRuntimeException) ex;
                        assertThat(sre.getStatus().getCode()).isEqualTo(INVALID_ARGUMENT);
                    });
        }

        @Test
        @DisplayName("Should return INTERNAL when application service throws unexpected exception")
        void shouldReturnInternalOnUnexpectedException() {
            // Arrange
            given(journalEntryApplicationService.createJournalEntry(any()))
                    .willThrow(new RuntimeException("Database connection lost"));

            CreateJournalEntryRequest request = validRequest("PAY-2026-08-0001");

            // Act & Assert
            assertThatThrownBy(() -> blockingStub.createJournalEntry(request))
                    .isInstanceOf(StatusRuntimeException.class)
                    .satisfies(ex -> {
                        StatusRuntimeException sre = (StatusRuntimeException) ex;
                        assertThat(sre.getStatus().getCode()).isEqualTo(INTERNAL);
                    });
        }
    }

    @Nested
    @DisplayName("CheckHealth RPC")
    class CheckHealthRpc {

        @Test
        @DisplayName("Should return UP status")
        void shouldReturnUpStatus() {
            // Act
            HealthCheckResponse response = blockingStub.checkHealth(
                    HealthCheckRequest.newBuilder()
                            .setService("payroll-service")
                            .build()
            );

            // Assert
            assertThat(response.getStatus()).isEqualTo("UP");
            assertThat(response.getVersion()).isNotBlank();
        }
    }

    private CreateJournalEntryRequest validRequest(String reference) {
        return CreateJournalEntryRequest.newBuilder()
                .setReference(reference)
                .setDescription("Payroll for period 2026-08")
                .setDebitAccount("SALARY_EXPENSE")
                .setCreditAccount("PAYROLL_PAYABLE")
                .setAmount("208333.33")
                .setCurrency("USD")
                .setSourceSystem("PAYROLL-SERVICE")
                .setEntryType(JournalEntryType.PAYROLL)
                .build();
    }
}
