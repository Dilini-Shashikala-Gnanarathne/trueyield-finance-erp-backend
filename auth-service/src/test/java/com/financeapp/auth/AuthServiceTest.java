package com.financeapp.auth;

import com.financeapp.auth.domain.entity.BuyerProfileEntity;
import com.financeapp.auth.domain.entity.FarmerProfileEntity;
import com.financeapp.auth.domain.entity.UserEntity;
import com.financeapp.auth.domain.enums.UserRole;
import com.financeapp.auth.domain.enums.UserStatus;
import com.financeapp.auth.dto.request.LoginRequest;
import com.financeapp.auth.dto.request.RegisterBuyerRequest;
import com.financeapp.auth.dto.request.RegisterFarmerRequest;
import com.financeapp.auth.dto.response.AuthResponse;
import com.financeapp.auth.exception.AccountNotActiveException;
import com.financeapp.auth.exception.DuplicateIdentifierException;
import com.financeapp.auth.exception.InvalidCredentialsException;
import com.financeapp.auth.repository.BuyerProfileRepository;
import com.financeapp.auth.repository.FarmerProfileRepository;
import com.financeapp.auth.repository.UserRepository;
import com.financeapp.auth.security.JwtUtil;
import com.financeapp.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FarmerProfileRepository farmerProfileRepository;

    @Mock
    private BuyerProfileRepository buyerProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        lenient().when(jwtUtil.getAccessTokenExpiryMs()).thenReturn(86400000L);
        lenient().when(jwtUtil.generateAccessToken(any(), any(), any(), any(), any())).thenReturn("mocked-jwt-token");
    }

    @Test
    @DisplayName("AUTH-001: Successfully register farmer with valid credentials and profile")
    void shouldRegisterFarmerSuccessfully() {
        RegisterFarmerRequest request = RegisterFarmerRequest.builder()
                .fullName("Sunil Bandara")
                .phone("0771234567")
                .email("sunil@example.com")
                .password("SecurePass123!")
                .farmName("Sunil Rambutan Gardens")
                .locality("Mirigama")
                .district("Gampaha")
                .latitude(new BigDecimal("7.2432000"))
                .longitude(new BigDecimal("80.1345000"))
                .build();

        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123!")).thenReturn("hashed-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        AuthResponse response = authService.registerFarmer(request);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getAccessToken());
        assertEquals("Sunil Bandara", response.getUser().getFullName());
        assertEquals("0771234567", response.getUser().getPhone());
        assertEquals(UserRole.FARMER, response.getUser().getRole());
        assertEquals(UserStatus.ACTIVE, response.getUser().getStatus());

        verify(farmerProfileRepository, times(1)).save(any(FarmerProfileEntity.class));
        verify(passwordEncoder, times(1)).encode("SecurePass123!");
    }

    @Test
    @DisplayName("AUTH-001: Reject farmer registration if phone already exists")
    void shouldRejectFarmerWithDuplicatePhone() {
        RegisterFarmerRequest request = RegisterFarmerRequest.builder()
                .fullName("Sunil Bandara")
                .phone("0771234567")
                .password("SecurePass123!")
                .build();

        when(userRepository.existsByPhone("0771234567")).thenReturn(true);

        assertThrows(DuplicateIdentifierException.class, () -> authService.registerFarmer(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("AUTH-002: Successfully register buyer")
    void shouldRegisterBuyerSuccessfully() {
        RegisterBuyerRequest request = RegisterBuyerRequest.builder()
                .fullName("Kamal Perera")
                .phone("0719876543")
                .email("kamal@example.com")
                .password("BuyerPass123!")
                .deliveryAddress("No 12, Main Street, Colombo")
                .locality("Colombo 03")
                .district("Colombo")
                .build();

        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("BuyerPass123!")).thenReturn("hashed-buyer-pass");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        AuthResponse response = authService.registerBuyer(request);

        assertNotNull(response);
        assertEquals("Kamal Perera", response.getUser().getFullName());
        assertEquals(UserRole.BUYER, response.getUser().getRole());
        assertEquals("0719876543", response.getUser().getPhone());

        verify(buyerProfileRepository, times(1)).save(any(BuyerProfileEntity.class));
    }

    @Test
    @DisplayName("AUTH-003: Successfully login with correct phone and password")
    void shouldLoginSuccessfully() {
        LoginRequest request = LoginRequest.builder()
                .identifier("0771234567")
                .password("SecurePass123!")
                .build();

        UserEntity user = UserEntity.builder()
                .id("farmer-id-123")
                .fullName("Sunil Bandara")
                .phone("0771234567")
                .email("sunil@example.com")
                .password("hashed-password")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .registeredAt(Instant.now())
                .build();

        when(userRepository.findByIdentifier("0771234567")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecurePass123!", "hashed-password")).thenReturn(true);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getAccessToken());
        assertEquals(UserRole.FARMER, response.getUser().getRole());
    }

    @Test
    @DisplayName("AUTH-003: Fail login with incorrect password")
    void shouldFailLoginWithBadPassword() {
        LoginRequest request = LoginRequest.builder()
                .identifier("0771234567")
                .password("WrongPassword")
                .build();

        UserEntity user = UserEntity.builder()
                .id("farmer-id-123")
                .password("hashed-password")
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByIdentifier("0771234567")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("AUTH-003: Fail login if user account is suspended")
    void shouldFailLoginIfAccountSuspended() {
        LoginRequest request = LoginRequest.builder()
                .identifier("0771234567")
                .password("SecurePass123!")
                .build();

        UserEntity user = UserEntity.builder()
                .id("farmer-id-123")
                .password("hashed-password")
                .status(UserStatus.SUSPENDED)
                .build();

        when(userRepository.findByIdentifier("0771234567")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecurePass123!", "hashed-password")).thenReturn(true);

        assertThrows(AccountNotActiveException.class, () -> authService.login(request));
    }
}
