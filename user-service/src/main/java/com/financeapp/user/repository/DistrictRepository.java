package com.financeapp.user.repository;

import com.financeapp.user.domain.entity.DistrictEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepository extends JpaRepository<DistrictEntity, Integer> {
}
