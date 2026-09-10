package com.financeapp.payroll.client;

import com.financeapp.payroll.dto.FinanceJournalRequest;
import com.financeapp.payroll.dto.FinanceJournalResponse;
import com.financeapp.payroll.exception.FinanceServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;

/**
 * REST implementation of {@link FinanceClient}.
 *
 * <p>This implementation is provided for <strong>benchmark comparison only</strong>.
 * It performs the same logical operation as {@link GrpcFinanceClient} but uses
 * HTTP/JSON instead of gRPC/Protobuf.</p>
 *
 * <h2>Purpose: Side-by-side benchmark</h2>
 * <p>By switching between {@code grpcFinanceClient} and {@code restFinanceClient}
 * in the PayrollService, we can measure:
 * <ul>
 *   <li>Serialization time: JSON vs Protobuf</li>
 *   <li>Network overhead: HTTP/1.1 vs HTTP/2</li>
 *   <li>Latency at p50, p95, p99</li>
 *   <li>Throughput under load</li>
 * </ul>
 * The business logic and database operations remain identical so that only
 * the transport layer differs.</p>
 *
 * <h2>Why NOT use REST for internal services in production?</h2>
 * <ul>
 *   <li>JSON parsing overhead on every request (CPU + memory)</li>
 *   <li>No compile-time contract enforcement (breaking changes are runtime errors)</li>
 *   <li>HTTP/1.1 connection overhead (head-of-line blocking)</li>
 *   <li>Manual timeout/retry configuration (gRPC has first-class deadline propagation)</li>
 * </ul>
 */
@Slf4j
@Component("restFinanceClient")
@RequiredArgsConstructor
public class RestFinanceClient implements FinanceClient {

    private final WebClient financeWebClient;

    @Value("${finance.rest.timeout-seconds:5}")
    private int timeoutSeconds;

    @Override
    public FinanceJournalResponse createJournalEntry(FinanceJournalRequest request) {
        log.info("Calling Finance Service via REST. reference={}, amount={}",
                request.getReference(), request.getAmount());

        try {
            Map<String, Object> requestBody = Map.of(
                    "reference", request.getReference(),
                    "description", request.getDescription(),
                    "debitAccount", request.getDebitAccount(),
                    "creditAccount", request.getCreditAccount(),
                    "amount", request.getAmount().toPlainString(),
                    "currency", request.getCurrency(),
                    "sourceSystem", request.getSourceSystem()
            );

            Map<?, ?> responseBody = financeWebClient.post()
                    .uri("/api/finance/journal-entries")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .map(body -> switch (clientResponse.statusCode().value()) {
                                        case 400 -> FinanceServiceException.invalidRequest(body);
                                        case 409 -> FinanceServiceException.alreadyExists(request.getReference(), body);
                                        default -> FinanceServiceException.invalidRequest(body);
                                    }))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .map(body -> FinanceServiceException.serviceUnavailable(
                                            "Finance REST Service error: " + body)))
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (responseBody == null) {
                throw FinanceServiceException.internalError("Finance REST Service returned empty response");
            }

            log.info("Finance Service REST call succeeded. journalReference={}",
                    responseBody.get("journalReference"));

            return FinanceJournalResponse.builder()
                    .journalReference((String) responseBody.get("journalReference"))
                    .status((String) responseBody.get("status"))
                    .message((String) responseBody.get("message"))
                    .wasIdempotent(Boolean.TRUE.equals(responseBody.get("wasIdempotent")))
                    .build();

        } catch (FinanceServiceException e) {
            throw e;
        } catch (WebClientResponseException.ServiceUnavailable e) {
            throw FinanceServiceException.serviceUnavailable(
                    "Finance REST Service is unavailable: " + e.getMessage());
        } catch (Exception e) {
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                throw FinanceServiceException.timeout(
                        "Finance REST Service timed out after " + timeoutSeconds + " seconds");
            }
            throw FinanceServiceException.internalError(
                    "Unexpected error calling Finance REST Service: " + e.getMessage());
        }
    }
}
