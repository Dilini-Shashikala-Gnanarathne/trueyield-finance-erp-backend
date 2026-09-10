package com.financeapp.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Gateway fallback controller for circuit-breaker scenarios.
 *
 * When a downstream service is unavailable, Spring Cloud Gateway's
 * CircuitBreaker filter forwards to this endpoint rather than returning
 * a raw connection error to the client.
 */
@RestController
@RequestMapping("/gateway")
public class GatewayFallbackController {

    @RequestMapping("/fallback")
    public ResponseEntity<Map<String, Object>> fallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 503,
                        "error", "Service Unavailable",
                        "message", "The requested service is temporarily unavailable. Please try again later.",
                        "path", "/gateway/fallback"
                ));
    }
}
