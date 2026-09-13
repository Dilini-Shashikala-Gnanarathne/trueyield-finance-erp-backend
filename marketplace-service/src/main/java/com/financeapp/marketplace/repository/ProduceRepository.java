package com.financeapp.marketplace.repository;

import com.financeapp.marketplace.domain.entity.ProduceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProduceRepository extends JpaRepository<ProduceEntity, String> {

    Optional<ProduceEntity> findByCode(String code);

    List<ProduceEntity> findByIsActiveTrueOrderByCodeAsc();

    boolean existsByCode(String code);
}
