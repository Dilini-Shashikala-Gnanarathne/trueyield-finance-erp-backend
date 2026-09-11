package com.accmaster.usersvc.service;

import com.accmaster.usersvc.domain.entity.PasswordPoolEntity;
import com.accmaster.usersvc.domain.entity.UserEntity;
import com.accmaster.usersvc.exception.BusinessException;
import com.accmaster.usersvc.repository.PasswordPoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Password lifecycle management service.
 *
 * Implements:
 *  - BCrypt encoding (replaces PHP password_hash(PASSWORD_DEFAULT))
 *  - password_verify equivalent (PasswordEncoder.matches())
 *  - Password history pool with last-3 check (matches PHP PasswordPoolRepository)
 *  - Trim pool to retain only the last PASSWORD_HISTORY_LIMIT entries
 */
@Service
@RequiredArgsConstructor
public class PasswordService {

    /** Matches PHP PasswordPoolRepository::PASSWORD_HISTORY_LIMIT = 3 */
    public static final int PASSWORD_HISTORY_LIMIT = 3;

    private final PasswordEncoder passwordEncoder;
    private final PasswordPoolRepository passwordPoolRepository;

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
     * Saves a new hashed password to the pool and trims old entries beyond the retention limit.
     * This is the equivalent of PHP insertPasswordAndTrimPool().
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
