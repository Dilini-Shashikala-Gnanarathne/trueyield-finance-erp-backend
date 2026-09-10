package com.financeapp.payroll.exception;

import lombok.Getter;

/**
 * Domain exception representing failures when communicating with Finance Service.
 *
 * <p>This exception carries an {@link ErrorType} that allows the
 * {@link GlobalExceptionHandler} to map it to the correct HTTP status code
 * without coupling the business layer to HTTP semantics.</p>
 *
 * <p>Factory methods are used instead of multiple subclasses for simplicity.
 * The error type enum carries the semantic meaning.</p>
 */
@Getter
public class FinanceServiceException extends RuntimeException {

    public enum ErrorType {
        INVALID_REQUEST,    // → HTTP 400
        ALREADY_EXISTS,     // → HTTP 409
        TIMEOUT,            // → HTTP 504
        UNAVAILABLE,        // → HTTP 503
        INTERNAL_ERROR      // → HTTP 500
    }

    private final ErrorType errorType;

    /** The existing reference in case of ALREADY_EXISTS, for idempotent returns. */
    private final String existingReference;

    private FinanceServiceException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.existingReference = null;
    }

    private FinanceServiceException(ErrorType errorType, String message, String existingReference) {
        super(message);
        this.errorType = errorType;
        this.existingReference = existingReference;
    }

    public static FinanceServiceException invalidRequest(String message) {
        return new FinanceServiceException(ErrorType.INVALID_REQUEST, message);
    }

    public static FinanceServiceException alreadyExists(String reference, String message) {
        return new FinanceServiceException(ErrorType.ALREADY_EXISTS,
                "Journal entry already exists for reference: " + reference, reference);
    }

    public static FinanceServiceException timeout(String message) {
        return new FinanceServiceException(ErrorType.TIMEOUT, message);
    }

    public static FinanceServiceException serviceUnavailable(String message) {
        return new FinanceServiceException(ErrorType.UNAVAILABLE, message);
    }

    public static FinanceServiceException internalError(String message) {
        return new FinanceServiceException(ErrorType.INTERNAL_ERROR, message);
    }
}
