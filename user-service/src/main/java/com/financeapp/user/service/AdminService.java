package com.financeapp.user.service;

import com.financeapp.user.domain.entity.AdminEntity;
import com.financeapp.user.domain.entity.ProfileEntity;
import com.financeapp.user.domain.entity.UserEntity;
import com.financeapp.user.domain.enums.AdminRole;
import com.financeapp.user.domain.enums.UserStatus;
import com.financeapp.user.domain.enums.UserType;
import com.financeapp.user.dto.request.CreateAdminRequest;
import com.financeapp.user.dto.request.DeleteUserRequest;
import com.financeapp.user.dto.request.UpdateAdminRequest;
import com.financeapp.user.dto.request.UpdateStatusRequest;
import com.financeapp.user.dto.response.AdminResponse;
import com.financeapp.user.dto.response.PagedResponse;
import com.financeapp.user.exception.DuplicateIdentifierException;
import com.financeapp.user.exception.InvalidCredentialsException;
import com.financeapp.user.exception.UserNotFoundException;
import com.financeapp.user.mapper.UserMapper;
import com.financeapp.user.repository.AdminRepository;
import com.financeapp.user.repository.ProfileRepository;
import com.financeapp.user.repository.UserRepository;
import com.financeapp.user.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service managing Administrator accounts and operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordService passwordService;
    private final UserMapper userMapper;

    /**
     * Creates a new administrator account (called by Super Admin).
     */
    @Transactional
    public AdminResponse createAdmin(CreateAdminRequest request, SecurityPrincipal principal) {
        log.info("Creating new admin with NIC: {} and mobile: {} by creator: {}",
                request.nic(), request.mobile(), principal.getUserId());

        Optional<UserEntity> existingUser = userRepository.findByMobileOrNic(request.mobile(), request.nic());
        if (existingUser.isPresent()) {
            UserEntity user = existingUser.get();
            if (user.getNic() != null && user.getNic().equalsIgnoreCase(request.nic())) {
                throw new DuplicateIdentifierException("A user with this NIC already exists.");
            }
            if (user.getMobile() != null && user.getMobile().equals(request.mobile())) {
                throw new DuplicateIdentifierException("A user with this mobile number already exists.");
            }
        }

        String userId = UUID.randomUUID().toString();
        String encodedPassword = passwordService.encode(request.password());

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername(request.nic()); // Admin username defaults to NIC
        user.setPassword(encodedPassword);
        user.setMobile(request.mobile());
        user.setNic(request.nic());
        user.setUserType(UserType.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setForcePasswordReset(true); // Require password reset on first login
        user.setRegisteredBy(principal.getUserId());
        user.setRegisteredAt(Instant.now());

        AdminEntity admin = new AdminEntity();
        admin.setUser(user);
        admin.setUserId(userId);
        admin.setFname(request.firstName());
        admin.setLname(request.lastName());
        admin.setRole(AdminRole.valueOf(request.role().toUpperCase()));
        admin.setUpdatedAt(Instant.now());
        admin.setUpdatedBy(principal.getUserId());

        user.setAdmin(admin);
        user = userRepository.save(user);

        // Record initial password in password pool
        passwordService.recordPassword(user.getId(), encodedPassword);
        log.info("Successfully created admin user: userId={}", userId);

        return userMapper.toAdminResponse(admin);
    }

    /**
     * Finds an admin by User ID.
     */
    @Transactional(readOnly = true)
    public AdminResponse getAdminById(String userId) {
        AdminEntity admin = adminRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found for ID: " + userId));
        return userMapper.toAdminResponse(admin);
    }

    /**
     * Lists all administrators with pagination.
     */
    @Transactional(readOnly = true)
    public PagedResponse<AdminResponse> listAdmins(Pageable pageable) {
        Page<AdminEntity> page = adminRepository.findAll(pageable);
        return PagedResponse.from(page.map(userMapper::toAdminResponse));
    }

    /**
     * Updates admin profile details.
     */
    @Transactional
    public AdminResponse updateAdmin(UpdateAdminRequest request, SecurityPrincipal principal) {
        String userId = request.id();
        log.info("Updating admin details for ID: {} by updater: {}", userId, principal.getUserId());

        AdminEntity admin = adminRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found for ID: " + userId));

        admin.setFname(request.firstName());
        admin.setLname(request.lastName());
        admin.setUpdatedAt(Instant.now());
        admin.setUpdatedBy(principal.getUserId());

        UserEntity user = admin.getUser();

        if (request.profileUrl() != null && !request.profileUrl().isBlank()) {
            ProfileEntity profile = user.getProfile();
            if (profile == null) {
                profile = new ProfileEntity();
                profile.setId(UUID.randomUUID().toString());
                profile.setUser(user);
                user.setProfile(profile);
            }
            profile.setUrl(request.profileUrl());
            profile.setUpdatedAt(Instant.now());
            profile.setUpdatedBy(principal.getUserId());
            profileRepository.save(profile);
        }

        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(principal.getUserId());
        userRepository.save(user);

        AdminEntity saved = adminRepository.save(admin);
        return userMapper.toAdminResponse(saved);
    }

    /**
     * Updates admin account status.
     */
    @Transactional
    public void updateStatus(UpdateStatusRequest request, SecurityPrincipal principal) {
        String userId = request.userId();
        log.info("Updating status for admin ID: {} to {}", userId, request.status());

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Admin user not found for ID: " + userId));

        UserStatus newStatus = UserStatus.valueOf(request.status().toUpperCase());
        user.setStatus(newStatus);
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(principal.getUserId());
        userRepository.save(user);
    }

    /**
     * Soft-deletes an admin account.
     */
    @Transactional
    public void deleteAdmin(DeleteUserRequest request, SecurityPrincipal principal) {
        String userId = request.userId();
        log.info("Soft deleting admin ID: {} requested by {}", userId, principal.getUserId());

        UserEntity targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found for ID: " + userId));

        UserEntity requestingUser = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Requesting user not found: " + principal.getUserId()));

        if (!passwordService.matches(request.password(), requestingUser.getPassword())) {
            throw new InvalidCredentialsException("Invalid password confirmation.");
        }

        targetUser.setDeletedBy(principal.getUserId());
        targetUser.setStatus(UserStatus.DELETED);
        userRepository.delete(targetUser);
    }
}
