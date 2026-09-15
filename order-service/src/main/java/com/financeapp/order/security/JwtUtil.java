package com.financeapp.order.security;

import com.financeapp.order.domain.enums.UserRole;
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
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.public-key-path:classpath:keys/public_key.pem}")
    private Resource publicKeyResource;

    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        this.publicKey = loadPublicKey(publicKeyResource);
        log.info("Order Service JWT RSA256 public key loaded successfully.");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public SecurityPrincipal extractPrincipal(String token) {
        Claims claims = extractAllClaims(token);
        String userId = claims.getSubject();
        String fullName = claims.get("fullName", String.class);
        String phone = claims.get("phone", String.class);
        String email = claims.get("email", String.class);
        String roleStr = claims.get("role", String.class);
        UserRole role = roleStr != null ? UserRole.valueOf(roleStr) : UserRole.BUYER;

        return new SecurityPrincipal(userId, fullName, phone, email, role);
    }

    private PublicKey loadPublicKey(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream()) {
            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(new X509EncodedKeySpec(decoded));
        }
    }
}
