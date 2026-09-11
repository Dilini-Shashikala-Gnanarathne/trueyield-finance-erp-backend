package com.financeapp.user.repository;

import com.financeapp.user.domain.entity.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CityRepository extends JpaRepository<CityEntity, Integer> {
    List<CityEntity> findByDistrictId(Integer districtId);
}
