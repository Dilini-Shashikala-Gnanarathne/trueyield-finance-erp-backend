package com.financeapp.auth.repository;

import com.financeapp.auth.domain.entity.BuyerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuyerProfileRepository extends JpaRepository<BuyerProfileEntity, String> {
    Optional<BuyerProfileEntity> findByUserId(String userId);
}
