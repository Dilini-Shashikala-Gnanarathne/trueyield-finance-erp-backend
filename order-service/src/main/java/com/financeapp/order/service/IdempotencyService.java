package com.financeapp.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.order.domain.entity.OrderIdempotencyEntity;
import com.financeapp.order.domain.enums.IdempotencyStatus;
import com.financeapp.order.dto.order.OrderResponse;
import com.financeapp.order.exception.IdempotencyConflictException;
import com.financeapp.order.repository.OrderIdempotencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;

/**
 * ORDER-005: Idempotent Order Creation with Idempotency-Key.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final OrderIdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Optional<OrderResponse> checkOrReserveKey(String key, String buyerId, Object request) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }

        String requestHash = hashRequest(request);

        Optional<OrderIdempotencyEntity> existing = idempotencyRepository.findByIdempotencyKeyAndBuyerId(key, buyerId);

        if (existing.isPresent()) {
            OrderIdempotencyEntity record = existing.get();

            if (record.getStatus() == IdempotencyStatus.COMPLETED && record.getResponsePayload() != null) {
                log.info("Idempotent hit for key={}, returning cached OrderResponse", key);
                try {
                    return Optional.of(objectMapper.readValue(record.getResponsePayload(), OrderResponse.class));
                } catch (Exception e) {
                    log.error("Failed to deserialize cached idempotency response: {}", e.getMessage());
                }
            } else if (record.getStatus() == IdempotencyStatus.PROCESSING) {
                throw new IdempotencyConflictException("An order request with Idempotency-Key '" + key + "' is currently being processed. Please wait.");
            }
        }

        // Reserve key in PROCESSING status
        OrderIdempotencyEntity entity = OrderIdempotencyEntity.builder()
                .idempotencyKey(key)
                .buyerId(buyerId)
                .requestHash(requestHash)
                .status(IdempotencyStatus.PROCESSING)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();

        idempotencyRepository.save(entity);
        return Optional.empty();
    }

    @Transactional
    public void markCompleted(String key, String buyerId, String orderId, OrderResponse response) {
        if (key == null || key.isBlank()) {
            return;
        }

        idempotencyRepository.findByIdempotencyKeyAndBuyerId(key, buyerId).ifPresent(record -> {
            try {
                record.setOrderId(orderId);
                record.setStatus(IdempotencyStatus.COMPLETED);
                record.setResponsePayload(objectMapper.writeValueAsString(response));
                idempotencyRepository.save(record);
            } catch (Exception e) {
                log.error("Failed to serialize order response for idempotency caching: {}", e.getMessage());
            }
        });
    }

    @Transactional
    public void markFailed(String key, String buyerId) {
        if (key == null || key.isBlank()) {
            return;
        }
        idempotencyRepository.findByIdempotencyKeyAndBuyerId(key, buyerId).ifPresent(record -> {
            record.setStatus(IdempotencyStatus.FAILED);
            idempotencyRepository.save(record);
        });
    }

    private String hashRequest(Object request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
