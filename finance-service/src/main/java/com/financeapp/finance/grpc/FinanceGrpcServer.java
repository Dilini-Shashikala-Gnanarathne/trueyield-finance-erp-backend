package com.financeapp.finance.grpc;

import com.financeapp.finance.dto.CreateJournalEntryCommand;
import com.financeapp.finance.dto.JournalEntryResult;
import com.financeapp.finance.grpc.proto.*;
import com.financeapp.finance.service.JournalEntryApplicationService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;

/**
 * gRPC server adapter for the Finance Service.
 *
 * <p>This class is the <em>adapter</em> (port) between the gRPC transport layer
 * and the business logic in {@link JournalEntryApplicationService}.
 * It is responsible for:
 * <ol>
 *   <li>Translating gRPC protobuf messages → internal command objects</li>
 *   <li>Delegating to the application service</li>
 *   <li>Translating results → gRPC protobuf response messages</li>
 *   <li>Mapping exceptions → appropriate gRPC status codes</li>
 * </ol>
 * No business logic belongs here.</p>
 *
 * <h2>The @GrpcService annotation</h2>
 * <p>The {@code @GrpcService} annotation (from net.devh starter) automatically:
 * <ul>
 *   <li>Registers this service with the gRPC server</li>
 *   <li>Applies configured interceptors (e.g., logging, auth)</li>
 *   <li>Makes it available on the configured gRPC port (default: 9090)</li>
 * </ul>
 * No manual ServerBuilder.addService() call needed.</p>
 *
 * <h2>gRPC Status Codes</h2>
 * <p>gRPC has its own set of status codes, separate from HTTP status codes.
 * Mapping them correctly is critical for clients to react appropriately:
 * <ul>
 *   <li>{@code INVALID_ARGUMENT} — bad input (like HTTP 400)</li>
 *   <li>{@code ALREADY_EXISTS} — idempotent duplicate (like HTTP 409)</li>
 *   <li>{@code INTERNAL} — unexpected server error (like HTTP 500)</li>
 * </ul>
 * </p>
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class FinanceGrpcServer extends FinanceServiceGrpc.FinanceServiceImplBase {

    private final JournalEntryApplicationService journalEntryApplicationService;

    /**
     * Handles CreateJournalEntry RPC calls from Payroll Service.
     *
     * <p>This is the primary gRPC endpoint. It demonstrates:
     * <ul>
     *   <li>Protobuf message parsing and validation</li>
     *   <li>Delegation to business logic layer</li>
     *   <li>Idempotent response handling</li>
     *   <li>Proper gRPC error status mapping</li>
     * </ul>
     * </p>
     */
    @Override
    public void createJournalEntry(
            CreateJournalEntryRequest request,
            StreamObserver<CreateJournalEntryResponse> responseObserver) {

        log.info("gRPC CreateJournalEntry called. reference={}, amount={}, currency={}",
                request.getReference(), request.getAmount(), request.getCurrency());

        try {
            // Step 1: Validate the protobuf request
            validateRequest(request);

            // Step 2: Convert proto message → internal command
            CreateJournalEntryCommand command = toCommand(request);

            // Step 3: Delegate to application service
            JournalEntryResult result = journalEntryApplicationService.createJournalEntry(command);

            // Step 4: Convert result → proto response
            CreateJournalEntryResponse response = toResponse(result);

            // Step 5: Send successful response
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC CreateJournalEntry completed. reference={}, wasIdempotent={}",
                    result.getReference(), result.isWasIdempotent());

        } catch (IllegalArgumentException e) {
            // Input validation failure → INVALID_ARGUMENT
            log.warn("gRPC request validation failed. reference={}, error={}",
                    request.getReference(), e.getMessage());
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );

        } catch (Exception e) {
            // Unexpected failure → INTERNAL
            log.error("Unexpected error in gRPC CreateJournalEntry. reference={}",
                    request.getReference(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal Finance Service error: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Health check RPC — useful for readiness probes and circuit breaker health checks.
     */
    @Override
    public void checkHealth(
            HealthCheckRequest request,
            StreamObserver<HealthCheckResponse> responseObserver) {

        log.debug("gRPC health check from: {}", request.getService());

        responseObserver.onNext(
                HealthCheckResponse.newBuilder()
                        .setStatus("UP")
                        .setVersion("1.0.0")
                        .build()
        );
        responseObserver.onCompleted();
    }

    /**
     * Validates required fields in the protobuf request.
     *
     * <p>Proto3 does not have required fields — all fields have zero-value defaults.
     * We must explicitly validate that business-critical fields are present.</p>
     */
    private void validateRequest(CreateJournalEntryRequest request) {
        if (request.getReference() == null || request.getReference().isBlank()) {
            throw new IllegalArgumentException("reference is required");
        }
        if (request.getDebitAccount() == null || request.getDebitAccount().isBlank()) {
            throw new IllegalArgumentException("debit_account is required");
        }
        if (request.getCreditAccount() == null || request.getCreditAccount().isBlank()) {
            throw new IllegalArgumentException("credit_account is required");
        }
        if (request.getAmount() == null || request.getAmount().isBlank()) {
            throw new IllegalArgumentException("amount is required");
        }
        if (request.getCurrency() == null || request.getCurrency().isBlank()) {
            throw new IllegalArgumentException("currency is required");
        }

        // Validate that amount is a valid positive decimal
        try {
            BigDecimal amount = new BigDecimal(request.getAmount());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("amount must be positive, got: " + request.getAmount());
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "amount is not a valid decimal number: " + request.getAmount());
        }
    }

    private CreateJournalEntryCommand toCommand(CreateJournalEntryRequest request) {
        return CreateJournalEntryCommand.builder()
                .reference(request.getReference())
                .description(request.getDescription())
                .debitAccount(request.getDebitAccount())
                .creditAccount(request.getCreditAccount())
                .amount(new BigDecimal(request.getAmount()))
                .currency(request.getCurrency())
                .sourceSystem(request.getSourceSystem())
                .entryType(request.getEntryType().name())
                .build();
    }

    private CreateJournalEntryResponse toResponse(JournalEntryResult result) {
        CreateJournalEntryResponse.Builder builder = CreateJournalEntryResponse.newBuilder()
                .setJournalReference(result.getReference())
                .setStatus(JournalStatus.CREATED)
                .setWasIdempotent(result.isWasIdempotent())
                .setMessage(result.getMessage());

        if (result.getCreatedAt() != null) {
            builder.setCreatedAt(result.getCreatedAt().toString());
        }

        return builder.build();
    }
}
