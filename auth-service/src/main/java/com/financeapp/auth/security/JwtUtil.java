package com.financeapp.auth.security;

import com.financeapp.auth.domain.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * JWT utility using RS256 (RSA Private/Public key pair).
 * Embeds user identity, full name, phone, email, and role claims.
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.private-key-path:classpath:keys/private_key.pem}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path:classpath:keys/public_key.pem}")
    private Resource publicKeyResource;

    @Value("${jwt.access-token-expiry-ms:86400000}")
    private long accessTokenExpiryMs;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        this.privateKey = loadPrivateKey(privateKeyResource);
        this.publicKey = loadPublicKey(publicKeyResource);
        log.info("JWT RSA256 keys loaded successfully.");
    }

    /**
     * Generates a signed RS256 JWT access token.
     */
    public String generateAccessToken(String userId, String fullName, String phone, String email, UserRole role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claim("fullName", fullName)
                .claim("phone", phone)
                .claim("email", email)
                .claim("role", role.name())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpiryMs)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public long getAccessTokenExpiryMs() {
        return accessTokenExpiryMs;
    }

    /**
     * Parses and validates a JWT token, returning its claims. Throws JwtException on failure.
     */
    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Converts validated claims into SecurityPrincipal.
     */
    public SecurityPrincipal toPrincipal(Claims claims) {
        String userId = claims.getSubject();
        String fullName = claims.get("fullName", String.class);
        String phone = claims.get("phone", String.class);
        String email = claims.get("email", String.class);
        String roleStr = claims.get("role", String.class);
        UserRole role = UserRole.valueOf(roleStr);
        return new SecurityPrincipal(userId, fullName, phone, email, role);
    }

    private PrivateKey loadPrivateKey(Resource resource) throws Exception {
        String pem = readPem(resource)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey(Resource resource) throws Exception {
        String pem = readPem(resource)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private String readPem(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
