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

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class FinanceGrpcServer extends FinanceServiceGrpc.FinanceServiceImplBase {

    private final JournalEntryApplicationService journalEntryApplicationService;
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
