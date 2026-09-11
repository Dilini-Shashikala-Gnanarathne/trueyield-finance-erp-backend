package com.accmaster.usersvc.service;

import com.accmaster.usersvc.domain.entity.BatchEntity;
import com.accmaster.usersvc.domain.entity.StudentEntity;
import com.accmaster.usersvc.domain.entity.UserEntity;
import com.accmaster.usersvc.domain.enums.AdminRole;
import com.accmaster.usersvc.domain.enums.BatchStatus;
import com.accmaster.usersvc.domain.enums.Gender;
import com.accmaster.usersvc.domain.enums.UserStatus;
import com.accmaster.usersvc.domain.enums.UserType;
import com.accmaster.usersvc.dto.request.LoginRequest;
import com.accmaster.usersvc.dto.request.PasswordResetCompleteRequest;
import com.accmaster.usersvc.dto.request.PasswordResetInitiateRequest;
import com.accmaster.usersvc.dto.request.RegisterStudentRequest;
import com.accmaster.usersvc.dto.request.VerifyOtpRequest;
import com.accmaster.usersvc.dto.response.AuthResponse;
import com.accmaster.usersvc.dto.response.OtpChallengeResponse;
import com.accmaster.usersvc.exception.AccountNotActiveException;
import com.accmaster.usersvc.exception.BusinessException;
import com.accmaster.usersvc.exception.DuplicateIdentifierException;
import com.accmaster.usersvc.exception.InvalidCredentialsException;
import com.accmaster.usersvc.exception.PasswordResetRequiredException;
import com.accmaster.usersvc.exception.UserNotFoundException;
import com.accmaster.usersvc.repository.BatchRepository;
import com.accmaster.usersvc.repository.UserRepository;
import com.accmaster.usersvc.security.JwtUtil;
import com.accmaster.usersvc.security.SecurityPrincipal;
import com.accmaster.usersvc.strategy.LoginStrategy;
import com.accmaster.usersvc.strategy.LoginStrategyFactory;
import com.accmaster.usersvc.strategy.OtpLoginStrategy;
import com.accmaster.usersvc.util.AcademicIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Core Authentication and Registration Service.
 * Implements end-to-end authentication, student self-registration, 2FA OTP flows,
 * and dual-method password reset with history guard.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String REGISTRATION_PURPOSE = "REGISTRATION";
    private static final String PASSWORD_RESET_PURPOSE = "PASSWORD_RESET";

    private final UserRepository userRepository;
    private final BatchRepository batchRepository;
    private final PasswordService passwordService;
    private final OtpService otpService;
    private final LoginStrategyFactory loginStrategyFactory;
    private final JwtUtil jwtUtil;
    private final AcademicIdGenerator academicIdGenerator;

    /**
     * Self-registration for students.
     * Matches PHP AuthService::registerStudent flow:
     * - Handles unverified student re-triggering registration with same mobile
     * - Checks for NIC or mobile collisions on active accounts
     * - Allocates academic ID & default active batch
     * - Stores credentials & hashes password
     * - Issues 6-digit OTP via Redis
     */
    @Transactional
    public OtpChallengeResponse registerStudent(RegisterStudentRequest request) {
        log.info("Processing student registration request for mobile: {}", request.mobile());

        Optional<UserEntity> existingByMobile = userRepository.findByMobile(request.mobile());
        Optional<UserEntity> existingByNic = userRepository.findByNic(request.nic());

        // Check NIC collision across all users
        if (existingByNic.isPresent()) {
            UserEntity nicUser = existingByNic.get();
            // If NIC belongs to another user (or verified student), throw error
            if (existingByMobile.isEmpty() || !nicUser.getId().equals(existingByMobile.get().getId())
                    || nicUser.getStatus() != UserStatus.NOT_VERIFIED) {
                throw new DuplicateIdentifierException("A user with this NIC already exists.");
            }
        }

        UserEntity user;
        String encodedPassword = passwordService.encode(request.password());

        if (existingByMobile.isPresent()) {
            UserEntity existingUser = existingByMobile.get();
            if (existingUser.getStatus() != UserStatus.NOT_VERIFIED) {
                throw new DuplicateIdentifierException("A user with this mobile number already exists.");
            }

            // Refresh existing NOT_VERIFIED student registration
            existingUser.setNic(request.nic());
            existingUser.setPassword(encodedPassword);
            existingUser.setUpdatedAt(Instant.now());

            StudentEntity student = existingUser.getStudent();
            if (student != null) {
                student.setFname(request.firstName());
                student.setLname(request.lastName());
                student.setUpdatedAt(Instant.now());
            }

            user = userRepository.save(existingUser);
            log.info("Refreshed unverified student account for user ID: {}", user.getId());
        } else {
            // Fresh registration flow
            BatchEntity defaultBatch = batchRepository.findFirstByStatus(BatchStatus.ACTIVE)
                    .orElseThrow(() -> new BusinessException("Default active batch not found in system."));

            String academicId = academicIdGenerator.generateAcademicId();
            String userId = UUID.randomUUID().toString();

            user = new UserEntity();
            user.setId(userId);
            user.setUsername(request.mobile());
            user.setPassword(encodedPassword);
            user.setMobile(request.mobile());
            user.setNic(request.nic());
            user.setUserType(UserType.STUDENT);
            user.setStatus(UserStatus.NOT_VERIFIED);
            user.setRegisteredBy(userId);
            user.setRegisteredAt(Instant.now());

            StudentEntity student = new StudentEntity();
            student.setUser(user);
            student.setUserId(userId);
            student.setAcademicId(academicId);
            student.setFname(request.firstName());
            student.setLname(request.lastName());
            student.setGender(Gender.NOT_SET);
            student.setBatch(defaultBatch);
            student.setUpdatedAt(Instant.now());

            user.setStudent(student);
            user = userRepository.save(user);

            // Record initial password in password pool
            passwordService.recordPassword(user.getId(), encodedPassword);
            log.info("Created new unverified student record with academic ID: {} and user ID: {}", academicId, userId);
        }

        // Generate & store OTP in Redis (TTL 300s)
        String otp = otpService.generateOtp();
        String txnId = otpService.storeOtp(REGISTRATION_PURPOSE, user.getId(), user.getUsername(), otp);

        return new OtpChallengeResponse(txnId, "OTP sent to your registered mobile number.");
    }

    /**
     * Verifies registration OTP and activates the student account.
     */
    @Transactional
    public AuthResponse verifyRegistrationOtp(VerifyOtpRequest request) {
        log.info("Verifying registration OTP for txnId: {}", request.txnId());

        Map<String, String> payload = otpService.verifyOtp(REGISTRATION_PURPOSE, request.txnId(), request.otp());
        String userId = payload.get("userId");

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        log.info("Student successfully activated: userId={}", userId);
        return buildAuthResponse(user);
    }

    /**
     * Initiates login flow using the Strategy pattern.
     */
    @Transactional(readOnly = true)
    public LoginStrategy.LoginResult login(LoginRequest request) {
        log.info("Login attempt for identifier: {}", request.username());

        UserEntity user = userRepository.findByUsername(request.username())
                .or(() -> userRepository.findByMobile(request.username()))
                .or(() -> userRepository.findByNic(request.username()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordService.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (user.getStatus() == UserStatus.NOT_VERIFIED) {
            String otp = otpService.generateOtp();
            String txnId = otpService.storeOtp(REGISTRATION_PURPOSE, user.getId(), user.getUsername(), otp);
            return new LoginStrategy.OtpResult(new OtpChallengeResponse(txnId, "Account not verified. OTP sent to registered mobile."));
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("Account is " + user.getStatus().name().toLowerCase() + ". Please contact administration.");
        }

        if (user.isForcePasswordReset()) {
            throw new PasswordResetRequiredException("Password reset is required before proceeding.");
        }

        return loginStrategyFactory.execute(user);
    }

    /**
     * Completes 2FA OTP login verification and issues JWT access token.
     */
    @Transactional(readOnly = true)
    public AuthResponse verifyLoginOtp(VerifyOtpRequest request) {
        log.info("Verifying 2FA login OTP for txnId: {}", request.txnId());

        Map<String, String> payload = otpService.verifyOtp(OtpLoginStrategy.getPurpose(), request.txnId(), request.otp());
        String userId = payload.get("userId");

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("Account is not active.");
        }

        return buildAuthResponse(user);
    }

    /**
     * Step 1 of password reset: Initiates reset via OTP or old password challenge.
     */
    public OtpChallengeResponse initiatePasswordReset(PasswordResetInitiateRequest request) {
        log.info("Initiating password reset for identifier: {}, method: {}", request.identifier(), request.verificationMethod());

        UserEntity user = userRepository.findByMobile(request.identifier())
                .orElseThrow(() -> new UserNotFoundException("User not found with mobile: " + request.identifier()));

        if ("OTP".equalsIgnoreCase(request.verificationMethod())) {
            String otp = otpService.generateOtp();
            String txnId = otpService.storeOtp(PASSWORD_RESET_PURPOSE, user.getId(), user.getUsername(), otp);
            return new OtpChallengeResponse(txnId, "Password reset OTP sent to registered mobile.");
        } else if ("OLD_PASSWORD".equalsIgnoreCase(request.verificationMethod())) {
            String txnId = UUID.randomUUID().toString();
            return new OtpChallengeResponse(txnId, "Proceed to provide your current password and new password.");
        } else {
            throw new BusinessException("Unsupported verification method: " + request.verificationMethod());
        }
    }

    /**
     * Step 2 of password reset: Verifies challenge, validates password history, and updates password.
     */
    @Transactional
    public void completePasswordReset(PasswordResetCompleteRequest request) {
        log.info("Completing password reset for identifier: {}, method: {}", request.identifier(), request.verificationMethod());

        UserEntity user = userRepository.findByMobile(request.identifier())
                .orElseThrow(() -> new UserNotFoundException("User not found with mobile: " + request.identifier()));

        if ("OTP".equalsIgnoreCase(request.verificationMethod())) {
            if (request.txnId() == null || request.otp() == null) {
                throw new BusinessException("txnId and otp are required for OTP verification method.");
            }
            otpService.verifyOtp(PASSWORD_RESET_PURPOSE, request.txnId(), request.otp());
        } else if ("OLD_PASSWORD".equalsIgnoreCase(request.verificationMethod())) {
            if (request.oldPassword() == null || request.oldPassword().isBlank()) {
                throw new BusinessException("oldPassword is required for OLD_PASSWORD verification method.");
            }
            if (!passwordService.matches(request.oldPassword(), user.getPassword())) {
                throw new InvalidCredentialsException("Current password does not match.");
            }
        } else {
            throw new BusinessException("Unsupported verification method: " + request.verificationMethod());
        }

        // Validate password is not reused from last 3 historical passwords
        passwordService.validateNotReused(user.getId(), request.newPassword());

        // Hash and update
        String newHash = passwordService.encode(request.newPassword());
        user.setPassword(newHash);
        user.setForcePasswordReset(false);
        user.setLastPasswordResetAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        // Record to history pool
        passwordService.recordPassword(user.getId(), newHash);
        log.info("Password reset successfully completed for user ID: {}", user.getId());
    }

    /**
     * Gets user profile info for the currently authenticated principal.
     */
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(SecurityPrincipal principal) {
        UserEntity user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + principal.getUserId()));
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(UserEntity user) {
        String role = resolveRole(user);
        String token = jwtUtil.generateAccessToken(user.getId(), role, user.getUserType());

        String fname = null;
        String lname = null;
        String academicId = null;
        String gender = null;

        if (user.getUserType() == UserType.STUDENT && user.getStudent() != null) {
            fname = user.getStudent().getFname();
            lname = user.getStudent().getLname();
            academicId = user.getStudent().getAcademicId();
            gender = user.getStudent().getGender() != null ? user.getStudent().getGender().name() : null;
        } else if (user.getUserType() == UserType.ADMIN && user.getAdmin() != null) {
            fname = user.getAdmin().getFname();
            lname = user.getAdmin().getLname();
        }

        String profileUrl = user.getProfile() != null ? user.getProfile().getUrl() : null;

        return new AuthResponse(
                token,
                user.getId(),
                role,
                user.getUserType().name(),
                fname,
                lname,
                user.getMobile(),
                user.getEmail(),
                user.getNic(),
                academicId,
                gender,
                profileUrl,
                user.isForcePasswordReset(),
                user.getRegisteredAt()
        );
    }

    private String resolveRole(UserEntity user) {
        if (user.getUserType() == UserType.ADMIN && user.getAdmin() != null) {
            AdminRole role = user.getAdmin().getRole();
            return role != null ? role.name() : "ADMIN";
        }
        return user.getUserType().name();
    }
}
