package com.financeapp.auth;

import com.financeapp.auth.domain.entity.FarmerProfileEntity;
import com.financeapp.auth.domain.entity.UserEntity;
import com.financeapp.auth.domain.enums.LocationVisibility;
import com.financeapp.auth.domain.enums.UserRole;
import com.financeapp.auth.domain.enums.UserStatus;
import com.financeapp.auth.dto.request.UpdateFarmerProfileRequest;
import com.financeapp.auth.dto.response.FarmerProfileResponse;
import com.financeapp.auth.dto.response.SellerPublicProfileResponse;
import com.financeapp.auth.repository.BuyerProfileRepository;
import com.financeapp.auth.repository.FarmerProfileRepository;
import com.financeapp.auth.repository.UserRepository;
import com.financeapp.auth.security.SecurityPrincipal;
import com.financeapp.auth.service.ProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FarmerProfileRepository farmerProfileRepository;

    @Mock
    private BuyerProfileRepository buyerProfileRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    @DisplayName("AUTH-005: Update farmer profile with new marketplace location and farm details")
    void shouldUpdateFarmerProfileSuccessfully() {
        SecurityPrincipal principal = new SecurityPrincipal("farmer-1", "Sunil", "0771234567", "sunil@example.com", UserRole.FARMER);

        UserEntity user = UserEntity.builder()
                .id("farmer-1")
                .fullName("Sunil Bandara")
                .phone("0771234567")
                .email("sunil@example.com")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .registeredAt(Instant.now())
                .build();

        FarmerProfileEntity profile = FarmerProfileEntity.builder()
                .id("prof-1")
                .user(user)
                .farmName("Old Farm")
                .locality("Mirigama")
                .locationVisibility(LocationVisibility.APPROXIMATE)
                .build();

        when(userRepository.findById("farmer-1")).thenReturn(Optional.of(user));
        when(farmerProfileRepository.findByUserId("farmer-1")).thenReturn(Optional.of(profile));
        when(farmerProfileRepository.save(any(FarmerProfileEntity.class))).thenAnswer(i -> i.getArgument(0));

        UpdateFarmerProfileRequest request = UpdateFarmerProfileRequest.builder()
                .farmName("Sunil Organic Rambutan Farm")
                .bio("Finest Malwana rambutan produce")
                .locality("Mirigama Central")
                .district("Gampaha")
                .latitude(new BigDecimal("7.243500"))
                .longitude(new BigDecimal("80.134700"))
                .locationVisibility(LocationVisibility.APPROXIMATE)
                .build();

        FarmerProfileResponse response = profileService.updateFarmerProfile(principal, request);

        assertNotNull(response);
        assertEquals("Sunil Organic Rambutan Farm", response.getFarmName());
        assertEquals("Mirigama Central", response.getLocality());
        assertEquals(new BigDecimal("7.243500"), response.getLatitude());
        assertNotNull(response.getLocationUpdatedAt());

        verify(farmerProfileRepository, times(1)).save(any(FarmerProfileEntity.class));
    }

    @Test
    @DisplayName("AUTH-005 & Map Section 5: Public seller profile rounds coordinates for APPROXIMATE visibility")
    void shouldRoundCoordinatesForApproximateLocationVisibility() {
        String sellerId = "farmer-1";

        UserEntity user = UserEntity.builder()
                .id(sellerId)
                .fullName("Sunil Bandara")
                .role(UserRole.FARMER)
                .build();

        FarmerProfileEntity profile = FarmerProfileEntity.builder()
                .id("prof-1")
                .user(user)
                .farmName("Sunil Gardens")
                .locality("Mirigama")
                .district("Gampaha")
                .latitude(new BigDecimal("7.2435123"))
                .longitude(new BigDecimal("80.1347890"))
                .locationVisibility(LocationVisibility.APPROXIMATE)
                .build();

        when(userRepository.findById(sellerId)).thenReturn(Optional.of(user));
        when(farmerProfileRepository.findByUserId(sellerId)).thenReturn(Optional.of(profile));

        SellerPublicProfileResponse response = profileService.getSellerPublicProfile(sellerId);

        assertNotNull(response);
        assertEquals("Sunil Bandara", response.getSellerName());
        assertEquals(new BigDecimal("7.24"), response.getLatitude());
        assertEquals(new BigDecimal("80.13"), response.getLongitude());
        assertEquals("Mirigama", response.getLocality());
    }

    @Test
    @DisplayName("AUTH-005 & Map Section 5: Public seller profile conceals coordinates for EXACT_AFTER_ORDER")
    void shouldConcealCoordinatesWhenExactAfterOrder() {
        String sellerId = "farmer-1";

        UserEntity user = UserEntity.builder()
                .id(sellerId)
                .fullName("Sunil Bandara")
                .role(UserRole.FARMER)
                .build();

        FarmerProfileEntity profile = FarmerProfileEntity.builder()
                .id("prof-1")
                .user(user)
                .farmName("Private Farm")
                .locality("Mirigama")
                .district("Gampaha")
                .latitude(new BigDecimal("7.2435123"))
                .longitude(new BigDecimal("80.1347890"))
                .locationVisibility(LocationVisibility.EXACT_AFTER_ORDER)
                .build();

        when(userRepository.findById(sellerId)).thenReturn(Optional.of(user));
        when(farmerProfileRepository.findByUserId(sellerId)).thenReturn(Optional.of(profile));

        SellerPublicProfileResponse response = profileService.getSellerPublicProfile(sellerId);

        assertNotNull(response);
        assertNull(response.getLatitude(), "Latitude should be hidden before order");
        assertNull(response.getLongitude(), "Longitude should be hidden before order");
        assertEquals("Mirigama", response.getLocality());
    }
}
