package com.financeapp.user.util;

import com.financeapp.user.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Year;

/**
 * Generates unique academic IDs following the format:
 * S + (currentYear - 2026) + 4-digit random number (e.g. S01234).
 * Mirrors PHP IDGenerator logic with duplicate collision checks.
 */
@Component
@RequiredArgsConstructor
public class AcademicIdGenerator {

    private static final String S_PREFIX = "S";
    private static final int BASE_YEAR = 2026;
    private static final int MAX_RETRIES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StudentRepository studentRepository;

    public String generateAcademicId() {
        int currentYear = Year.now().getValue();
        int yearPrefix = currentYear - BASE_YEAR;

        if (yearPrefix < 0 || yearPrefix > 9) {
            yearPrefix = Math.abs(yearPrefix) % 10;
        }

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            int randomVal = RANDOM.nextInt(10000);
            String aid = String.format("%s%d%04d", S_PREFIX, yearPrefix, randomVal);
            if (!studentRepository.existsByAcademicId(aid)) {
                return aid;
            }
        }

        throw new IllegalStateException("Failed to generate a unique academic ID after " + MAX_RETRIES + " attempts.");
    }
}
