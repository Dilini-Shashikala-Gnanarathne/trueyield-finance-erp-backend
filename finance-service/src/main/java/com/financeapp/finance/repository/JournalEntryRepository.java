package com.financeapp.finance.repository;

import com.financeapp.finance.domain.JournalEntryEntity;
import com.financeapp.finance.domain.JournalEntryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntryEntity, Long> {

    /**
     * Find a journal entry by its business reference.
     * Used for idempotency checks — if a reference already exists, return it.
     */
    Optional<JournalEntryEntity> findByReference(String reference);

    /**
     * Check whether a journal entry with the given reference exists.
     * Faster than findByReference when the full entity is not needed.
     */
    boolean existsByReference(String reference);

    Page<JournalEntryEntity> findByStatus(JournalEntryStatus status, Pageable pageable);

    Page<JournalEntryEntity> findByFarmerIdOrderByCreatedAtDesc(String farmerId, Pageable pageable);

    Page<JournalEntryEntity> findByBuyerIdOrderByCreatedAtDesc(String buyerId, Pageable pageable);
}
