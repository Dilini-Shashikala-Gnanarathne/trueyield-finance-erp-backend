package com.financeapp.marketplace.service;

import com.financeapp.marketplace.domain.entity.ProduceEntity;
import com.financeapp.marketplace.dto.produce.CreateProduceRequest;
import com.financeapp.marketplace.dto.produce.ProduceResponse;
import com.financeapp.marketplace.exception.BusinessException;
import com.financeapp.marketplace.exception.ResourceNotFoundException;
import com.financeapp.marketplace.repository.ProduceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing the produce catalogue (MARKET-001).
 * Rambutan is the initial seeded produce.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProduceService {

    private final ProduceRepository produceRepository;

    @Transactional(readOnly = true)
    public List<ProduceResponse> getAllActiveProduce() {
        return produceRepository.findByIsActiveTrueOrderByCodeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProduceResponse getProduceById(String id) {
        ProduceEntity produce = produceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produce not found with ID: " + id));
        return toResponse(produce);
    }

    @Transactional(readOnly = true)
    public ProduceResponse getProduceByCode(String code) {
        ProduceEntity produce = produceRepository.findByCode(code.toUpperCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Produce not found with code: " + code));
        return toResponse(produce);
    }

    @Transactional
    public ProduceResponse createProduce(CreateProduceRequest request) {
        String code = request.getCode().toUpperCase().trim();
        if (produceRepository.existsByCode(code)) {
            throw new BusinessException("Produce code already exists: " + code);
        }

        String supportedUnitsStr = (request.getSupportedUnits() != null && !request.getSupportedUnits().isEmpty())
                ? String.join(",", request.getSupportedUnits())
                : request.getDefaultUnit();

        String supportedGradesStr = (request.getSupportedGrades() != null && !request.getSupportedGrades().isEmpty())
                ? String.join(",", request.getSupportedGrades())
                : "PREMIUM,STANDARD,PROCESSING";

        ProduceEntity entity = ProduceEntity.builder()
                .id(UUID.randomUUID().toString())
                .code(code)
                .name(request.getName().trim())
                .scientificName(request.getScientificName())
                .category(request.getCategory())
                .description(request.getDescription())
                .defaultUnit(request.getDefaultUnit().toUpperCase().trim())
                .supportedUnits(supportedUnitsStr)
                .supportedGrades(supportedGradesStr)
                .imageUrl(request.getImageUrl())
                .isActive(true)
                .build();

        ProduceEntity saved = produceRepository.save(entity);
        log.info("Created new produce catalogue type: code={}, name={}, id={}", saved.getCode(), saved.getName(), saved.getId());
        return toResponse(saved);
    }

    public ProduceResponse toResponse(ProduceEntity entity) {
        return ProduceResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .scientificName(entity.getScientificName())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .defaultUnit(entity.getDefaultUnit())
                .supportedUnits(entity.getSupportedUnitsList())
                .supportedGrades(entity.getSupportedGradesList())
                .imageUrl(entity.getImageUrl())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
