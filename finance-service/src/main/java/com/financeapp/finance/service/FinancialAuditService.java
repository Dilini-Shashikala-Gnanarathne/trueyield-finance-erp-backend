package com.financeapp.finance.service;

import com.financeapp.finance.domain.JournalEntryAuditEntity;
import com.financeapp.finance.domain.JournalEntryEntity;
import com.financeapp.finance.repository.JournalEntryAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Service that records tamper-evident, cryptographically chained audit records
 * for all financial general ledger movements.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialAuditService {

    private final JournalEntryAuditRepository auditRepository;
    private static final String GENESIS_HASH = "0".repeat(64);

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordEntryAudit(JournalEntryEntity entry, String action, String performedBy) {
        try {
            String prevHash = auditRepository.findTopByOrderByIdDesc()
                    .map(JournalEntryAuditEntity::getEntryHash)
                    .orElse(GENESIS_HASH);

            String dataToHash = prevHash + "|" +
                    entry.getReference() + "|" +
                    entry.getTotalAmount() + "|" +
                    entry.getCurrency() + "|" +
                    action + "|" +
                    performedBy;

            String currentHash = calculateSha256(dataToHash);

            JournalEntryAuditEntity auditEntity = JournalEntryAuditEntity.builder()
                    .reference(entry.getReference())
                    .action(action)
                    .entryHash(currentHash)
                    .previousHash(prevHash)
                    .performedBy(performedBy)
                    .build();

            auditRepository.save(auditEntity);
            log.info("Chained financial audit recorded for journal {}: hash={}", entry.getReference(), currentHash);
        } catch (Exception ex) {
            log.error("Failed to write financial audit entry for reference {}: {}", entry.getReference(), ex.getMessage(), ex);
            throw new IllegalStateException("Audit trail generation failed", ex);
        }
    }

    private String calculateSha256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashBytes);
    }
}
