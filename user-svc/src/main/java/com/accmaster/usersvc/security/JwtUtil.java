package com.accmaster.usersvc.security;

import com.accmaster.usersvc.domain.enums.UserType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * JWT utility using RS256 (RSA Private/Public key pair).
 *
 * Token claims:
 *   sub   = userId
 *   role  = user role (SUPER_ADMIN, SUB_ADMIN, STUDENT)
 *   type  = user type (ADMIN, STUDENT)
 *   jti   = unique token ID (for future revocation)
 *
 * Keys must be placed in src/main/resources/keys/private_key.pem and public_key.pem
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    @Value("${jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        this.privateKey = loadPrivateKey(privateKeyResource);
        this.publicKey  = loadPublicKey(publicKeyResource);
    }

    /** Generates a signed RS256 JWT access token. */
    public String generateAccessToken(String userId, String role, UserType userType) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .claim("type", userType.name())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpiryMs)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /** Parses and validates a JWT token, returning its claims. Throws JwtException on failure. */
    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Extracts the SecurityPrincipal from validated JWT claims. */
    public SecurityPrincipal toPrincipal(Claims claims) {
        String userId   = claims.getSubject();
        String role     = claims.get("role", String.class);
        String typeStr  = claims.get("type", String.class);
        UserType type   = UserType.valueOf(typeStr);
        return new SecurityPrincipal(userId, userId, role, type);
    }

    // ---- Key loading helpers ----

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
