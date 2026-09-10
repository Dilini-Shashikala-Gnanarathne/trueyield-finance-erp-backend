package com.financeapp.payroll.client;

import com.financeapp.finance.grpc.proto.*;
import com.financeapp.payroll.dto.FinanceJournalRequest;
import com.financeapp.payroll.dto.FinanceJournalResponse;
import com.financeapp.payroll.exception.FinanceServiceException;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

/**
 * gRPC implementation of {@link FinanceClient}.
 *
 * <p>This is the primary implementation used in production. It communicates
 * with Finance Service using gRPC + Protocol Buffers over HTTP/2.</p>
 *
 * <h2>Why gRPC for internal communication?</h2>
 * <ul>
 *   <li><strong>Performance:</strong> Protobuf serialization is 3-10x smaller and
 *       faster than JSON. HTTP/2 multiplexing reduces connection overhead.</li>
 *   <li><strong>Strong contracts:</strong> The .proto file is the single source of truth.
 *       Breaking changes are detected at compile time.</li>
 *   <li><strong>Streaming:</strong> gRPC supports bidirectional streaming for future use cases.</li>
 *   <li><strong>Deadlines:</strong> gRPC has first-class support for propagating deadlines
 *       across service boundaries — critical for preventing cascading failures.</li>
 * </ul>
 *
 * <h2>Deadline and Retry Strategy</h2>
 * <p>Deadlines are configured in application.yml under {@code grpc.client.finance-service.deadline}.
 * Retries are handled conservatively — only UNAVAILABLE errors are retried, and only
 * because the Finance Service's CreateJournalEntry operation is idempotent.
 * We do NOT retry ALREADY_EXISTS, INVALID_ARGUMENT, or INTERNAL — those are not transient.</p>
 *
 * <h2>Error Mapping</h2>
 * <pre>
 *   gRPC INVALID_ARGUMENT  → FinanceServiceException (HTTP 400)
 *   gRPC ALREADY_EXISTS    → FinanceServiceException (HTTP 409) - idempotent, return existing
 *   gRPC DEADLINE_EXCEEDED → FinanceServiceException (HTTP 504)
 *   gRPC UNAVAILABLE       → FinanceServiceException (HTTP 503)
 *   gRPC INTERNAL          → FinanceServiceException (HTTP 500)
 * </pre>
 */
@Slf4j
@Component("grpcFinanceClient")
public class GrpcFinanceClient implements FinanceClient {

    /**
     * The @GrpcClient annotation injects a pre-configured blocking stub.
     * "finance-service" matches the key in application.yml:
     *   grpc.client.finance-service.address=...
     *
     * The net.devh starter configures the ManagedChannel automatically,
     * applying TLS settings, keep-alive, deadline, and retry policies
     * from application.yml — no manual channel management needed.
     */
    @GrpcClient("finance-service")
    private FinanceServiceGrpc.FinanceServiceBlockingStub financeServiceStub;

    @Override
    public FinanceJournalResponse createJournalEntry(FinanceJournalRequest request) {
        log.info("Calling Finance Service via gRPC. reference={}, amount={}",
                request.getReference(), request.getAmount());

        CreateJournalEntryRequest grpcRequest = buildGrpcRequest(request);

        try {
            CreateJournalEntryResponse grpcResponse = financeServiceStub.createJournalEntry(grpcRequest);

            log.info("Finance Service gRPC call succeeded. journalReference={}, wasIdempotent={}",
                    grpcResponse.getJournalReference(), grpcResponse.getWasIdempotent());

            return FinanceJournalResponse.builder()
                    .journalReference(grpcResponse.getJournalReference())
                    .status(grpcResponse.getStatus().name())
                    .message(grpcResponse.getMessage())
                    .wasIdempotent(grpcResponse.getWasIdempotent())
                    .build();

        } catch (StatusRuntimeException e) {
            return handleGrpcError(e, request.getReference());
        }
    }

    private CreateJournalEntryRequest buildGrpcRequest(FinanceJournalRequest request) {
        return CreateJournalEntryRequest.newBuilder()
                .setReference(request.getReference())
                .setDescription(request.getDescription())
                .setDebitAccount(request.getDebitAccount())
                .setCreditAccount(request.getCreditAccount())
                .setAmount(request.getAmount().toPlainString())
                .setCurrency(request.getCurrency())
                .setSourceSystem(request.getSourceSystem())
                .setEntryType(JournalEntryType.PAYROLL)
                .build();
    }

    private FinanceJournalResponse handleGrpcError(StatusRuntimeException e, String reference) {
        String code = e.getStatus().getCode().name();
        String description = e.getStatus().getDescription();

        log.error("Finance Service gRPC call failed. reference={}, code={}, description={}",
                reference, code, description);

        throw switch (e.getStatus().getCode()) {
            case INVALID_ARGUMENT ->
                    FinanceServiceException.invalidRequest(
                            "Finance Service rejected the journal request: " + description);

            case ALREADY_EXISTS -> {
                // Idempotent: the journal already exists. We surface this as a 409
                // but include the existing reference so the caller can handle it gracefully.
                log.info("Journal entry already exists (idempotent). reference={}", reference);
                yield FinanceServiceException.alreadyExists(reference, description);
            }

            case DEADLINE_EXCEEDED ->
                    FinanceServiceException.timeout(
                            "Finance Service did not respond within the deadline. The journal entry " +
                            "may or may not have been created. reference=" + reference);

            case UNAVAILABLE ->
                    FinanceServiceException.serviceUnavailable(
                            "Finance Service is currently unavailable. Please retry later. " +
                            "reference=" + reference);

            case NOT_FOUND ->
                    FinanceServiceException.invalidRequest(
                            "Requested resource not found in Finance Service: " + description);

            default ->
                    FinanceServiceException.internalError(
                            "Unexpected error from Finance Service: [" + code + "] " + description);
        };
    }
}
