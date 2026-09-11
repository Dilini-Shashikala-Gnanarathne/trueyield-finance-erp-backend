package com.accmaster.usersvc.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler — ZERO try-catch in controllers.
 * Returns RFC 7807 ProblemDetail for all error responses.
 *
 * Replaces PHP pattern of:
 *   } catch (Exception $e) { ResponseHelper::send(400, $e->getMessage()); }
 * in every controller method.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ---- Domain Exceptions ----

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        return buildProblem(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage());
    }

    @ExceptionHandler(DuplicateIdentifierException.class)
    public ProblemDetail handleDuplicate(DuplicateIdentifierException ex) {
        return buildProblem(HttpStatus.CONFLICT, "Duplicate Identifier", ex.getMessage());
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ProblemDetail handleInvalidOtp(InvalidOtpException ex) {
        return buildProblem(HttpStatus.UNAUTHORIZED, "Invalid OTP", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildProblem(HttpStatus.UNAUTHORIZED, "Invalid Credentials", ex.getMessage());
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ProblemDetail handleAccountNotActive(AccountNotActiveException ex) {
        return buildProblem(HttpStatus.FORBIDDEN, "Account Not Active", ex.getMessage());
    }

    @ExceptionHandler(PasswordResetRequiredException.class)
    public ProblemDetail handlePasswordResetRequired(PasswordResetRequiredException ex) {
        ProblemDetail pd = buildProblem(HttpStatus.FORBIDDEN, "Password Reset Required", ex.getMessage());
        pd.setProperty("errorCode", "RESET_PASSWORD");
        return pd;
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        return buildProblem(HttpStatus.BAD_REQUEST, "Business Rule Violation", ex.getMessage());
    }

    // ---- Validation Exceptions ----

    /** Handles @Valid failures on request bodies. Returns all field errors. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        ProblemDetail pd = buildProblem(HttpStatus.BAD_REQUEST, "Validation Failed", errors);
        pd.setProperty("fields", ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
                        (e1, e2) -> e1)));
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        String errors = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));
        return buildProblem(HttpStatus.BAD_REQUEST, "Constraint Violation", errors);
    }

    // ---- Security Exceptions ----

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return buildProblem(HttpStatus.FORBIDDEN, "Forbidden", "Insufficient permissions.");
    }

    // ---- Catch-all ----

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.");
    }

    // ---- Builder ----

    private ProblemDetail buildProblem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create("about:blank"));
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
