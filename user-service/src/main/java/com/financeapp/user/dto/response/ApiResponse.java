package com.financeapp.user.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
/**
 * Standard API response envelope.
 * Replaces PHP ResponseHelper::send(status, message, data).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    String timestamp
) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now().toString());
    }
    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, message, null, Instant.now().toString());
    }
}
