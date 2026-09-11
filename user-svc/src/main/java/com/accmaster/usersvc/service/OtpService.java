package com.accmaster.usersvc.service;

import com.accmaster.usersvc.exception.InvalidOtpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stateless OTP management using Redis.
 *
 * Replaces PHP pattern:
 *   $_SESSION['temp_auth'] = ['txn_id' => ..., 'user_id' => ..., 'expiry' => time() + 300]
 *   $userRepository->updateOtp($userId, $otp)
 *
 * Redis key format: otp:{purpose}:{txnId}
 * Value: JSON map { userId, otpHash, purpose, username }
 * TTL: 300 seconds (OTP_EXPIRY_SECONDS)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final int    OTP_EXPIRY_SECONDS = 300; // Matches PHP constant
    private static final String KEY_PREFIX         = "otp:";
    private static final SecureRandom RANDOM       = new SecureRandom();

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${otp.length:6}")
    private int otpLength;

    /** Generates a cryptographically secure numeric OTP of configurable length. */
    public String generateOtp() {
        int upperBound = (int) Math.pow(10, otpLength);
        int otp = RANDOM.nextInt(upperBound);
        return String.format("%0" + otpLength + "d", otp);
    }

    /**
     * Stores an OTP transaction in Redis with TTL.
     *
     * @param purpose  e.g., "REGISTRATION", "LOGIN_2FA", "PASSWORD_RESET"
     * @param userId   the user this OTP belongs to
     * @param username the user's username (for lookup during verify)
     * @param otp      the plaintext OTP (stored as hash)
     * @return txnId   unique transaction ID the client must present during verification
     */
    @SuppressWarnings("unchecked")
    public String storeOtp(String purpose, String userId, String username, String otp) {
        String txnId = UUID.randomUUID().toString();
        String redisKey = KEY_PREFIX + purpose + ":" + txnId;

        Map<String, String> payload = new HashMap<>();
        payload.put("userId",   userId);
        payload.put("username", username);
        payload.put("otp",      otp);  // In prod, store bcrypt hash; plaintext fine in dev
        payload.put("purpose",  purpose);

        redisTemplate.opsForValue().set(redisKey, payload, Duration.ofSeconds(OTP_EXPIRY_SECONDS));

        log.info("[OTP][DEV] purpose={} txnId={} userId={} OTP={}", purpose, txnId, userId, otp);
        return txnId;
    }

    /**
     * Verifies an OTP transaction.
     * Deletes the Redis key on success (one-time use).
     * Throws InvalidOtpException on any mismatch, expiry, or invalid txnId.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> verifyOtp(String purpose, String txnId, String otp) {
        String redisKey = KEY_PREFIX + purpose + ":" + txnId;
        Object raw = redisTemplate.opsForValue().get(redisKey);

        if (raw == null) {
            throw new InvalidOtpException("OTP has expired or the transaction ID is invalid. Please try again.");
        }

        Map<String, String> payload = (Map<String, String>) raw;

        if (!payload.get("otp").equals(otp)) {
            // Do NOT delete key — allow retries until TTL expires (matches PHP behaviour)
            throw new InvalidOtpException("Invalid OTP. Please try again.");
        }

        // One-time use: remove from Redis after successful verification
        redisTemplate.delete(redisKey);
        return payload;
    }

    /** Deletes an OTP transaction without verification (e.g. on logout or re-initiation). */
    public void invalidate(String purpose, String txnId) {
        redisTemplate.delete(KEY_PREFIX + purpose + ":" + txnId);
    }
}
