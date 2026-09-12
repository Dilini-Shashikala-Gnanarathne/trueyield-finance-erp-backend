package com.financeapp.auth.service;

import com.financeapp.auth.domain.entity.BuyerProfileEntity;
import com.financeapp.auth.domain.entity.FarmerProfileEntity;
import com.financeapp.auth.domain.entity.UserEntity;
import com.financeapp.auth.domain.enums.LocationVisibility;
import com.financeapp.auth.domain.enums.UserRole;
import com.financeapp.auth.dto.request.UpdateBuyerProfileRequest;
import com.financeapp.auth.dto.request.UpdateFarmerProfileRequest;
import com.financeapp.auth.dto.response.BuyerProfileResponse;
import com.financeapp.auth.dto.response.FarmerProfileResponse;
import com.financeapp.auth.dto.response.SellerPublicProfileResponse;
import com.financeapp.auth.dto.response.UserSummary;
import com.financeapp.auth.exception.BusinessException;
import com.financeapp.auth.exception.DuplicateIdentifierException;
import com.financeapp.auth.exception.ResourceNotFoundException;
import com.financeapp.auth.repository.BuyerProfileRepository;
import com.financeapp.auth.repository.FarmerProfileRepository;
import com.financeapp.auth.repository.UserRepository;
import com.financeapp.auth.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * AUTH-005: Profile Management Service.
 * Handles viewing and updating farmer, buyer, and marketplace seller profile information.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;
    private final FarmerProfileRepository farmerProfileRepository;
    private final BuyerProfileRepository buyerProfileRepository;

    /**
     * Retrieves current authenticated user's profile.
     */
    @Transactional(readOnly = true)
    public Object getMyProfile(SecurityPrincipal principal) {
        UserEntity user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == UserRole.FARMER) {
            FarmerProfileEntity profile = farmerProfileRepository.findByUserId(user.getId())
                    .orElseGet(() -> createDefaultFarmerProfile(user));
            return toFarmerProfileResponse(user, profile);
        } else if (user.getRole() == UserRole.BUYER) {
            BuyerProfileEntity profile = buyerProfileRepository.findByUserId(user.getId())
                    .orElseGet(() -> createDefaultBuyerProfile(user));
            return toBuyerProfileResponse(user, profile);
        } else {
            return toUserSummary(user);
        }
    }

    /**
     * Updates farmer marketplace and personal profile.
     */
    @Transactional
    public FarmerProfileResponse updateFarmerProfile(SecurityPrincipal principal, UpdateFarmerProfileRequest request) {
        UserEntity user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != UserRole.FARMER) {
            throw new BusinessException("Only users with role FARMER can update a farmer profile");
        }

        // Update personal user details
        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName().trim());
        }
        if (StringUtils.hasText(request.getEmail())) {
            String email = request.getEmail().trim().toLowerCase();
            if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new DuplicateIdentifierException("Email '" + email + "' is already in use by another account.");
            }
            user.setEmail(email);
        }

        FarmerProfileEntity profile = farmerProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultFarmerProfile(user));

        if (request.getFarmName() != null) {
            profile.setFarmName(request.getFarmName().trim());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio().trim());
        }
        if (request.getLocality() != null) {
            profile.setLocality(request.getLocality().trim());
        }
        if (request.getDistrict() != null) {
            profile.setDistrict(request.getDistrict().trim());
        }
        if (request.getLatitude() != null && request.getLongitude() != null) {
            profile.setLatitude(request.getLatitude());
            profile.setLongitude(request.getLongitude());
            profile.setLocationUpdatedAt(Instant.now());
        }
        if (request.getLocationVisibility() != null) {
            profile.setLocationVisibility(request.getLocationVisibility());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl().trim());
        }

        userRepository.save(user);
        profile = farmerProfileRepository.save(profile);

        log.info("Farmer profile updated for userId={}", user.getId());
        return toFarmerProfileResponse(user, profile);
    }

    /**
     * Updates buyer profile.
     */
    @Transactional
    public BuyerProfileResponse updateBuyerProfile(SecurityPrincipal principal, UpdateBuyerProfileRequest request) {
        UserEntity user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != UserRole.BUYER) {
            throw new BusinessException("Only users with role BUYER can update a buyer profile");
        }

        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName().trim());
        }
        if (StringUtils.hasText(request.getEmail())) {
            String email = request.getEmail().trim().toLowerCase();
            if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new DuplicateIdentifierException("Email '" + email + "' is already in use by another account.");
            }
            user.setEmail(email);
        }

        BuyerProfileEntity profile = buyerProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultBuyerProfile(user));

        if (request.getDeliveryAddress() != null) {
            profile.setDeliveryAddress(request.getDeliveryAddress().trim());
        }
        if (request.getLocality() != null) {
            profile.setLocality(request.getLocality().trim());
        }
        if (request.getDistrict() != null) {
            profile.setDistrict(request.getDistrict().trim());
        }
        if (request.getPreferredContactMethod() != null) {
            profile.setPreferredContactMethod(request.getPreferredContactMethod().trim());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl().trim());
        }

        userRepository.save(user);
        profile = buyerProfileRepository.save(profile);

        log.info("Buyer profile updated for userId={}", user.getId());
        return toBuyerProfileResponse(user, profile);
    }

    /**
     * Public marketplace seller profile endpoint.
     * Enforces privacy controls (Section 5 & 5.2):
     * - Protects exact coordinates by rounding to 2 decimal places (~1km approximate) if APPROXIMATE.
     */
    @Transactional(readOnly = true)
    public SellerPublicProfileResponse getSellerPublicProfile(String sellerId) {
        UserEntity user = userRepository.findById(sellerId)
                .filter(u -> u.getRole() == UserRole.FARMER)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));

        FarmerProfileEntity profile = farmerProfileRepository.findByUserId(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));

        BigDecimal safeLat = profile.getLatitude();
        BigDecimal safeLng = profile.getLongitude();

        // Privacy rule: Never expose exact home address by default (Section 5.2)
        if (profile.getLocationVisibility() == LocationVisibility.APPROXIMATE) {
            if (safeLat != null) {
                safeLat = safeLat.setScale(2, RoundingMode.HALF_UP);
            }
            if (safeLng != null) {
                safeLng = safeLng.setScale(2, RoundingMode.HALF_UP);
            }
        } else if (profile.getLocationVisibility() == LocationVisibility.EXACT_AFTER_ORDER) {
            // Conceal coordinates completely until order is confirmed
            safeLat = null;
            safeLng = null;
        }

        return SellerPublicProfileResponse.builder()
                .sellerId(user.getId())
                .sellerName(user.getFullName())
                .farmName(profile.getFarmName())
                .bio(profile.getBio())
                .locality(profile.getLocality())
                .district(profile.getDistrict())
                .latitude(safeLat)
                .longitude(safeLng)
                .locationVisibility(profile.getLocationVisibility())
                .avatarUrl(profile.getAvatarUrl())
                .build();
    }

    private FarmerProfileEntity createDefaultFarmerProfile(UserEntity user) {
        FarmerProfileEntity entity = FarmerProfileEntity.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .locationVisibility(LocationVisibility.APPROXIMATE)
                .createdAt(Instant.now())
                .build();
        return farmerProfileRepository.save(entity);
    }

    private BuyerProfileEntity createDefaultBuyerProfile(UserEntity user) {
        BuyerProfileEntity entity = BuyerProfileEntity.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .preferredContactMethod("PHONE")
                .createdAt(Instant.now())
                .build();
        return buyerProfileRepository.save(entity);
    }

    private UserSummary toUserSummary(UserEntity user) {
        return UserSummary.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .registeredAt(user.getRegisteredAt())
                .build();
    }

    private FarmerProfileResponse toFarmerProfileResponse(UserEntity user, FarmerProfileEntity profile) {
        return FarmerProfileResponse.builder()
                .profileId(profile.getId())
                .user(toUserSummary(user))
                .farmName(profile.getFarmName())
                .bio(profile.getBio())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .locality(profile.getLocality())
                .district(profile.getDistrict())
                .locationVisibility(profile.getLocationVisibility())
                .locationUpdatedAt(profile.getLocationUpdatedAt())
                .avatarUrl(profile.getAvatarUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private BuyerProfileResponse toBuyerProfileResponse(UserEntity user, BuyerProfileEntity profile) {
        return BuyerProfileResponse.builder()
                .profileId(profile.getId())
                .user(toUserSummary(user))
                .deliveryAddress(profile.getDeliveryAddress())
                .locality(profile.getLocality())
                .district(profile.getDistrict())
                .preferredContactMethod(profile.getPreferredContactMethod())
                .avatarUrl(profile.getAvatarUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
