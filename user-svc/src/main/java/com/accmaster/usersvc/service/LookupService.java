package com.accmaster.usersvc.service;

import com.accmaster.usersvc.domain.entity.BatchEntity;
import com.accmaster.usersvc.domain.entity.CityEntity;
import com.accmaster.usersvc.domain.entity.DistrictEntity;
import com.accmaster.usersvc.domain.enums.BatchStatus;
import com.accmaster.usersvc.repository.BatchRepository;
import com.accmaster.usersvc.repository.CityRepository;
import com.accmaster.usersvc.repository.DistrictRepository;
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
