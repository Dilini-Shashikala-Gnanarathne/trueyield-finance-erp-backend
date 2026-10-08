package com.financeapp.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * Distributed rate limiter and key resolution configuration powered by Redis.
 */
@Configuration
public class RateLimiterConfig {

    /**
     * Resolves client identity by client remote IP or X-Forwarded-For header.
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                String clientIp = forwardedFor.split(",")[0].trim();
                return Mono.just("ip:" + clientIp);
            }
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            String host = (remoteAddress != null && remoteAddress.getAddress() != null)
                    ? remoteAddress.getAddress().getHostAddress()
                    : "anonymous";
            return Mono.just("ip:" + host);
        };
    }

    /**
     * Resolves authenticated user ID from Authorization Bearer token header or falls back to IP.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return Mono.just("token:" + authHeader.substring(7, Math.min(32, authHeader.length())));
            }
            return ipKeyResolver().resolve(exchange);
        };
    }

    /**
     * Standard API rate limiter: 50 requests per second replenishment with burst capacity of 100.
     */
    @Bean
    @Primary
    public RedisRateLimiter defaultRedisRateLimiter() {
        return new RedisRateLimiter(50, 100, 1);
    }

    /**
     * Strict rate limiter for sensitive authentication endpoints (e.g. login/register brute-force protection).
     */
    @Bean
    public RedisRateLimiter authRedisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }
}
