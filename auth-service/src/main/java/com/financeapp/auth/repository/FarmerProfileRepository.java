package com.financeapp.auth.repository;

import com.financeapp.auth.domain.entity.FarmerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FarmerProfileRepository extends JpaRepository<FarmerProfileEntity, String> {
    Optional<FarmerProfileEntity> findByUserId(String userId);
}
