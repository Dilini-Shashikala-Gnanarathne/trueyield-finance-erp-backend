package com.financeapp.user.repository;

import com.financeapp.user.domain.entity.BatchEntity;
import com.financeapp.user.domain.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<BatchEntity, String> {
    boolean existsByNameAndIdNot(String name, String id);
    boolean existsByName(String name);
    Optional<BatchEntity> findFirstByStatus(BatchStatus status);
    List<BatchEntity> findAllByStatusNot(BatchStatus status);
}
