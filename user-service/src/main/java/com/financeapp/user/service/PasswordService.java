package com.financeapp.user.service;

import com.financeapp.user.domain.entity.PasswordPoolEntity;
import com.financeapp.user.domain.entity.UserEntity;
import com.financeapp.user.exception.BusinessException;
import com.financeapp.user.repository.PasswordPoolRepository;
import com.financeapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Password lifecycle management service.
 *
 * Implements:
 *  - BCrypt encoding
 *  - password_verify equivalent (PasswordEncoder.matches())
 *  - Password history pool with last-3 check
 *  - Trim pool to retain only the last PASSWORD_HISTORY_LIMIT entries
 */
@Service
@RequiredArgsConstructor
public class PasswordService {

    public static final int PASSWORD_HISTORY_LIMIT = 3;

    private final PasswordEncoder passwordEncoder;
    private final PasswordPoolRepository passwordPoolRepository;
    private final UserRepository userRepository;

    /** Encodes a raw password using BCrypt strength 12. */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /** Verifies a raw password against a BCrypt hash. */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Checks whether the new password has been used in the last PASSWORD_HISTORY_LIMIT passwords.
     * Throws BusinessException if it's a recently used password.
     */
    public void assertNotRecentlyUsed(String userId, String rawNewPassword) {
        List<PasswordPoolEntity> recent = passwordPoolRepository.findRecentByUserId(userId, PASSWORD_HISTORY_LIMIT);
        for (PasswordPoolEntity entry : recent) {
            if (passwordEncoder.matches(rawNewPassword, entry.getPassword())) {
                throw new BusinessException(
                        "This password has been used recently. Please choose a different password."
                );
            }
        }
    }

    /**
     * Validates that the new password was not previously used.
     */
    public void validateNotReused(String userId, String rawNewPassword) {
        assertNotRecentlyUsed(userId, rawNewPassword);
    }

    /**
     * Records a new hashed password in the pool for the given userId.
     */
    public void recordPassword(String userId, String encodedPassword) {
        UserEntity userRef = userRepository.getReferenceById(userId);
        saveToPool(userRef, encodedPassword);
    }

    /**
     * Saves a new hashed password to the pool and trims old entries beyond the retention limit.
     */
    public void saveToPool(UserEntity user, String encodedPassword) {
        PasswordPoolEntity entry = new PasswordPoolEntity();
        entry.setId(UUID.randomUUID().toString());
        entry.setUser(user);
        entry.setPassword(encodedPassword);
        passwordPoolRepository.save(entry);

        // Trim to keep only the most recent PASSWORD_HISTORY_LIMIT entries
        if (passwordPoolRepository.countByUserEntityId(user.getId()) > PASSWORD_HISTORY_LIMIT) {
            passwordPoolRepository.trimPool(user.getId(), PASSWORD_HISTORY_LIMIT);
        }
    }
}
