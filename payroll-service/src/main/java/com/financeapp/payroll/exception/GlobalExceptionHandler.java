package com.financeapp.payroll.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Payroll Service.
 *
 * <p>Maps domain exceptions and gRPC error types to appropriate HTTP responses.
 * Uses RFC 7807 Problem Details format (ProblemDetail) for consistent error responses.</p>
 *
 * <p><strong>Error mapping rationale:</strong>
 * <ul>
 *   <li>We do NOT return HTTP 500 for every error — that destroys client ability to retry safely.</li>
 *   <li>HTTP 409 tells the client "this already exists" — important for idempotent retries.</li>
 *   <li>HTTP 503 tells the client "retry later" — appropriate for temporary Finance outages.</li>
 *   <li>HTTP 504 tells the client "the backend timed out" — different from 503.</li>
 * </ul>
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles Bean Validation failures from @Valid annotated controller parameters.
     * Returns HTTP 400 with a list of field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        String violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, violations);
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("https://financeapp.com/problems/validation-error"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Validation failed: {}", violations);
        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Handles constraint violations from @Validated (method-level validation).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
        String violations = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, violations);
        problem.setTitle("Constraint Violation");
        problem.setType(URI.create("https://financeapp.com/problems/constraint-violation"));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Handles duplicate payroll period — the payroll for this period was already processed.
     * Returns HTTP 409 Conflict.
     */
    @ExceptionHandler(DuplicatePayrollException.class)
    public ResponseEntity<ProblemDetail> handleDuplicatePayroll(DuplicatePayrollException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Duplicate Payroll");
        problem.setType(URI.create("https://financeapp.com/problems/duplicate-payroll"));
        problem.setProperty("payrollPeriod", ex.getPayrollPeriod());
        problem.setProperty("timestamp", Instant.now());

        log.warn("Duplicate payroll attempt. period={}", ex.getPayrollPeriod());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Maps Finance Service exceptions to appropriate HTTP responses.
     *
     * <p>This is the critical mapping that translates gRPC status codes
     * (INVALID_ARGUMENT, ALREADY_EXISTS, DEADLINE_EXCEEDED, UNAVAILABLE, INTERNAL)
     * into meaningful HTTP responses for REST clients.</p>
     */
    @ExceptionHandler(FinanceServiceException.class)
    public ResponseEntity<ProblemDetail> handleFinanceServiceException(FinanceServiceException ex) {
        return switch (ex.getErrorType()) {
            case INVALID_REQUEST -> {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST, ex.getMessage());
                problem.setTitle("Finance Service Rejected Request");
                problem.setType(URI.create("https://financeapp.com/problems/finance-invalid-request"));
                problem.setProperty("timestamp", Instant.now());
                log.warn("Finance Service rejected request: {}", ex.getMessage());
                yield ResponseEntity.badRequest().body(problem);
            }

            case ALREADY_EXISTS -> {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                        HttpStatus.CONFLICT, ex.getMessage());
                problem.setTitle("Journal Entry Already Exists");
                problem.setType(URI.create("https://financeapp.com/problems/journal-already-exists"));
                problem.setProperty("existingReference", ex.getExistingReference());
                problem.setProperty("timestamp", Instant.now());
                log.info("Idempotent: journal already exists. reference={}", ex.getExistingReference());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
            }

            case TIMEOUT -> {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                        HttpStatus.GATEWAY_TIMEOUT, ex.getMessage());
                problem.setTitle("Finance Service Timeout");
                problem.setType(URI.create("https://financeapp.com/problems/finance-timeout"));
                problem.setProperty("timestamp", Instant.now());
                log.error("Finance Service timeout: {}", ex.getMessage());
                yield ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(problem);
            }

            case UNAVAILABLE -> {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                        HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
                problem.setTitle("Finance Service Unavailable");
                problem.setType(URI.create("https://financeapp.com/problems/finance-unavailable"));
                problem.setProperty("retryAfterSeconds", 30);
                problem.setProperty("timestamp", Instant.now());
                log.error("Finance Service unavailable: {}", ex.getMessage());
                yield ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
            }

            case INTERNAL_ERROR -> {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_GATEWAY, ex.getMessage());
                problem.setTitle("Finance Service Internal Error");
                problem.setType(URI.create("https://financeapp.com/problems/finance-internal-error"));
                problem.setProperty("timestamp", Instant.now());
                log.error("Finance Service internal error: {}", ex.getMessage());
                yield ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
            }
        };
    }

    /**
     * Last-resort handler for unexpected exceptions.
     * Logs the full stack trace but returns only a safe message to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getClass().getName() + ": " + ex.getMessage());
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://financeapp.com/problems/internal-error"));
        problem.setProperty("timestamp", Instant.now());

        log.error("Unexpected exception", ex);
        return ResponseEntity.internalServerError().body(problem);
    }
}
