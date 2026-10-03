package com.financeapp.finance.repository;

import com.financeapp.finance.domain.JournalEntryAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JournalEntryAuditRepository extends JpaRepository<JournalEntryAuditEntity, Long> {

    Optional<JournalEntryAuditEntity> findTopByOrderByIdDesc();
}
