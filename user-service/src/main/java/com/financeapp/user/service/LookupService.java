package com.financeapp.user.service;

import com.financeapp.user.domain.entity.BatchEntity;
import com.financeapp.user.domain.entity.CityEntity;
import com.financeapp.user.domain.entity.DistrictEntity;
import com.financeapp.user.domain.enums.BatchStatus;
import com.financeapp.user.repository.BatchRepository;
import com.financeapp.user.repository.CityRepository;
import com.financeapp.user.repository.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service providing lookup data: active batches, districts, and cities.
 */
@Service
@RequiredArgsConstructor
public class LookupService {

    private final BatchRepository batchRepository;
    private final DistrictRepository districtRepository;
    private final CityRepository cityRepository;

    @Transactional(readOnly = true)
    public List<BatchEntity> getActiveBatches() {
        return batchRepository.findAllByStatusNot(BatchStatus.DELETED);
    }

    @Transactional(readOnly = true)
    public List<DistrictEntity> getAllDistricts() {
        return districtRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<CityEntity> getCitiesByDistrict(Integer districtId) {
        return cityRepository.findByDistrictId(districtId);
    }
}
