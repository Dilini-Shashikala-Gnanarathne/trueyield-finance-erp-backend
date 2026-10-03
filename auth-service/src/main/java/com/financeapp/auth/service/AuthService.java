package com.financeapp.auth.service;

import com.financeapp.auth.domain.entity.BuyerProfileEntity;
import com.financeapp.auth.domain.entity.FarmerProfileEntity;
import com.financeapp.auth.domain.entity.UserEntity;
import com.financeapp.auth.domain.enums.LocationVisibility;
import com.financeapp.auth.domain.enums.UserRole;
import com.financeapp.auth.domain.enums.UserStatus;
import com.financeapp.auth.dto.request.LoginRequest;
import com.financeapp.auth.dto.request.RegisterBuyerRequest;
import com.financeapp.auth.dto.request.RegisterFarmerRequest;
import com.financeapp.auth.dto.response.AuthResponse;
import com.financeapp.auth.dto.response.UserSummary;
import com.financeapp.auth.exception.AccountNotActiveException;
import com.financeapp.auth.exception.DuplicateIdentifierException;
import com.financeapp.auth.exception.InvalidCredentialsException;
import com.financeapp.auth.repository.BuyerProfileRepository;
import com.financeapp.auth.repository.FarmerProfileRepository;
import com.financeapp.auth.repository.UserRepository;
import com.financeapp.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final FarmerProfileRepository farmerProfileRepository;
    private final BuyerProfileRepository buyerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * AUTH-001: Register Farmer account.
     */
    @Transactional
    public AuthResponse registerFarmer(RegisterFarmerRequest request) {
        String phone = normalizePhone(request.getPhone());
        String email = normalizeEmail(request.getEmail());

        log.info("Processing farmer registration for phone: {}", maskIdentifier(phone));

        validateUniqueness(phone, email);

        String userId = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        UserEntity user = UserEntity.builder()
                .id(userId)
                .phone(phone)
                .email(email)
                .password(encodedPassword)
                .fullName(request.getFullName().trim())
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .registeredAt(Instant.now())
                .registeredBy(userId)
                .build();

        user = userRepository.save(user);

        FarmerProfileEntity profile = FarmerProfileEntity.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .farmName(request.getFarmName() != null ? request.getFarmName().trim() : null)
                .locality(request.getLocality() != null ? request.getLocality().trim() : null)
                .district(request.getDistrict() != null ? request.getDistrict().trim() : null)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationVisibility(LocationVisibility.APPROXIMATE)
                .locationUpdatedAt((request.getLatitude() != null && request.getLongitude() != null) ? Instant.now() : null)
                .createdAt(Instant.now())
                .build();

        profile = farmerProfileRepository.save(profile);
        user.setFarmerProfile(profile);

        log.info("Farmer account created successfully: userId={}, phone={}", userId, maskIdentifier(phone));

        return buildAuthResponse(user);
    }

    /**
     * AUTH-002: Register Buyer account.
     */
    @Transactional
    public AuthResponse registerBuyer(RegisterBuyerRequest request) {
        String phone = normalizePhone(request.getPhone());
        String email = normalizeEmail(request.getEmail());

        log.info("Processing buyer registration for phone: {}", maskIdentifier(phone));

        validateUniqueness(phone, email);

        String userId = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        UserEntity user = UserEntity.builder()
                .id(userId)
                .phone(phone)
                .email(email)
                .password(encodedPassword)
                .fullName(request.getFullName().trim())
                .role(UserRole.BUYER)
                .status(UserStatus.ACTIVE)
                .registeredAt(Instant.now())
                .registeredBy(userId)
                .build();

        user = userRepository.save(user);

        BuyerProfileEntity profile = BuyerProfileEntity.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .deliveryAddress(request.getDeliveryAddress() != null ? request.getDeliveryAddress().trim() : null)
                .locality(request.getLocality() != null ? request.getLocality().trim() : null)
                .district(request.getDistrict() != null ? request.getDistrict().trim() : null)
                .preferredContactMethod("PHONE")
                .createdAt(Instant.now())
                .build();

        profile = buyerProfileRepository.save(profile);
        user.setBuyerProfile(profile);

        log.info("Buyer account created successfully: userId={}, phone={}", userId, maskIdentifier(phone));

        return buildAuthResponse(user);
    }

    /**
     * AUTH-003: Login with phone or email identifier and password.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        log.info("Authenticating login request for identifier: {}", maskIdentifier(identifier));

        UserEntity user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid phone/email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for user: {}", user.getId());
            throw new InvalidCredentialsException("Invalid phone/email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login attempt for non-active user: status={}, userId={}", user.getStatus(), user.getId());
            throw new AccountNotActiveException("Account is " + user.getStatus() + ". Please contact support.");
        }

        log.info("Login successful for user: id={}, role={}", user.getId(), user.getRole());
        return buildAuthResponse(user);
    }

    private void validateUniqueness(String phone, String email) {
        if (userRepository.existsByPhone(phone)) {
            throw new DuplicateIdentifierException("A user with phone number '" + phone + "' already exists.");
        }
        if (StringUtils.hasText(email) && userRepository.existsByEmail(email)) {
            throw new DuplicateIdentifierException("A user with email '" + email + "' already exists.");
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String cleaned = phone.replaceAll("\\s+", "");
        if (cleaned.startsWith("+94")) {
            cleaned = "0" + cleaned.substring(3);
        }
        return cleaned;
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) return null;
        return email.trim().toLowerCase();
    }

    private AuthResponse buildAuthResponse(UserEntity user) {
        String token = jwtUtil.generateAccessToken(
                user.getId(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole()
        );

        UserSummary summary = UserSummary.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .registeredAt(user.getRegisteredAt())
                .build();

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiryMs() / 1000)
                .user(summary)
                .build();
    }

    /**
     * AUTH-007: Invalidate user session on logout via Redis token blacklist.
     */
    public void logout(String authHeader) {
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String token = authHeader.substring(7).trim();
        try {
            io.jsonwebtoken.Claims claims = jwtUtil.validateAndGetClaims(token);
            tokenBlacklistService.blacklistToken(claims);
            log.info("User logged out successfully, token invalidated: jti={}", claims.getId());
        } catch (Exception ex) {
            log.warn("Failed to blacklist token during logout: {}", ex.getMessage());
        }
    }

    @Transactional
    public void updateUserStatus(String userId, UserStatus newStatus) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new com.financeapp.auth.exception.ResourceNotFoundException("User not found"));
        user.setStatus(newStatus);
        userRepository.save(user);
        tokenBlacklistService.setUserStatus(userId, newStatus.name());
        log.info("Admin updated user {} status to {}", userId, newStatus);
    }

    private String maskIdentifier(String input) {
        if (!StringUtils.hasText(input)) return "***";
        int len = input.length();
        if (len <= 4) return "****";
        if (input.contains("@")) {
            int atIndex = input.indexOf('@');
            if (atIndex <= 2) return "***@" + input.substring(atIndex + 1);
            return input.substring(0, 2) + "***@" + input.substring(atIndex + 1);
        }
        return input.substring(0, 3) + "****" + input.substring(len - 2);
    }
}
