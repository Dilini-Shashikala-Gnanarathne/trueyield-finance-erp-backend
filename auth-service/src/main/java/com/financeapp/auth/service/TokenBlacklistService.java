package com.financeapp.auth.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing active token blacklists and real-time user status revocation via Redis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String USER_STATUS_PREFIX = "user:status:";

    /**
     * Invalidate a token until its natural expiration time.
     */
    public void blacklistToken(Claims claims) {
        if (claims == null) return;
        try {
            String jti = claims.getId();
            Date expiration = claims.getExpiration();
            if (jti != null && expiration != null) {
                long remainingMs = expiration.getTime() - System.currentTimeMillis();
                if (remainingMs > 0) {
                    redisTemplate.opsForValue().set(
                            BLACKLIST_PREFIX + jti,
                            "revoked",
                            remainingMs,
                            TimeUnit.MILLISECONDS
                    );
                    log.info("Token blacklisted successfully: jti={}, remainingMs={}", jti, remainingMs);
                }
            }
        } catch (Exception ex) {
            log.warn("Could not blacklist token in Redis: {}", ex.getMessage());
        }
    }

    /**
     * Checks if a token ID has been revoked.
     */
    public boolean isTokenBlacklisted(String jti) {
        if (jti == null) return false;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
        } catch (Exception ex) {
            log.warn("Redis error checking token blacklist: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Cache user status in Redis for immediate real-time revocation across instances.
     */
    public void setUserStatus(String userId, String status) {
        if (userId == null || status == null) return;
        try {
            redisTemplate.opsForValue().set(
                    USER_STATUS_PREFIX + userId,
                    status,
                    24,
                    TimeUnit.HOURS
            );
            log.info("Cached user status in Redis: userId={}, status={}", userId, status);
        } catch (Exception ex) {
            log.warn("Could not set user status in Redis: {}", ex.getMessage());
        }
    }

    /**
     * Returns true if the user has been suspended.
     */
    public boolean isUserSuspended(String userId) {
        if (userId == null) return false;
        try {
            String status = redisTemplate.opsForValue().get(USER_STATUS_PREFIX + userId);
            return "SUSPENDED".equalsIgnoreCase(status);
        } catch (Exception ex) {
            return false;
        }
    }
}
