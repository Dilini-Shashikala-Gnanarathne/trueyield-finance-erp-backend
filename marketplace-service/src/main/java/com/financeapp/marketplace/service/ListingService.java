package com.financeapp.marketplace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.marketplace.client.AuthServiceClient;
import com.financeapp.marketplace.domain.entity.ListingEntity;
import com.financeapp.marketplace.domain.entity.ListingImageEntity;
import com.financeapp.marketplace.domain.entity.MarketplaceOutboxEntity;
import com.financeapp.marketplace.domain.entity.ProduceEntity;
import com.financeapp.marketplace.domain.enums.ListingStatus;
import com.financeapp.marketplace.domain.enums.LocationVisibility;
import com.financeapp.marketplace.domain.enums.UserRole;
import com.financeapp.marketplace.dto.PageResponse;
import com.financeapp.marketplace.dto.listing.*;
import com.financeapp.marketplace.dto.produce.ProduceResponse;
import com.financeapp.marketplace.exception.BusinessException;
import com.financeapp.marketplace.exception.ResourceNotFoundException;
import com.financeapp.marketplace.repository.ListingImageRepository;
import com.financeapp.marketplace.repository.ListingRepository;
import com.financeapp.marketplace.repository.ListingSpecification;
import com.financeapp.marketplace.repository.MarketplaceOutboxRepository;
import com.financeapp.marketplace.repository.ProduceRepository;
import com.financeapp.marketplace.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingService {

    private final ListingRepository listingRepository;
    private final ProduceRepository produceRepository;
    private final ListingImageRepository listingImageRepository;
    private final MarketplaceOutboxRepository outboxRepository;
    private final ListingValidationService validationService;
    private final ProduceService produceService;
    private final AuthServiceClient authServiceClient;
    private final ObjectMapper objectMapper;

    /**
     * MARKET-002: Create Listing in DRAFT state for authenticated farmer.
     */
    @Transactional
    public ListingResponse createListing(CreateListingRequest request, SecurityPrincipal principal) {
        ProduceEntity produce = produceRepository.findById(request.getProduceId())
                .orElseThrow(() -> new ResourceNotFoundException("Produce not found with ID: " + request.getProduceId()));

        String listingId = UUID.randomUUID().toString();

        String title = (request.getTitle() != null && !request.getTitle().isBlank())
                ? request.getTitle().trim()
                : "Fresh " + produce.getName() + " (" + request.getQualityGrade() + ")";

        ListingEntity listing = ListingEntity.builder()
                .id(listingId)
                .farmerId(principal.getUserId())
                .produce(produce)
                .title(title)
                .description(request.getDescription())
                .totalQuantity(request.getTotalQuantity())
                .availableQuantity(request.getTotalQuantity())
                .reservedQuantity(BigDecimal.ZERO)
                .unit(request.getUnit().toUpperCase().trim())
                .pricePerUnit(request.getPricePerUnit())
                .qualityGrade(request.getQualityGrade())
                .harvestDate(request.getHarvestDate())
                .status(ListingStatus.DRAFT)
                .minOrderQuantity(request.getMinOrderQuantity())
                .latitude(request.getLocation().getLatitude())
                .longitude(request.getLocation().getLongitude())
                .locality(request.getLocation().getLocality().trim())
                .district(request.getLocation().getDistrict())
                .locationVisibility(request.getLocation().getVisibility() != null
                        ? request.getLocation().getVisibility()
                        : LocationVisibility.APPROXIMATE)
                .images(new ArrayList<>())
                .build();

        // MARKET-007: Attach initial image URLs if provided
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            int order = 1;
            for (String url : request.getImageUrls()) {
                ListingImageEntity img = ListingImageEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .imageUrl(url.trim())
                        .isPrimary(order == 1)
                        .displayOrder(order++)
                        .build();
                listing.addImage(img);
            }
        }

        ListingEntity saved = listingRepository.save(listing);
        recordOutboxEvent("ListingCreated", saved);

        log.info("Created new listing draft: id={}, produce={}, farmerId={}", saved.getId(), produce.getName(), principal.getUserId());
        return toListingResponse(saved, principal);
    }

    /**
     * MARKET-003: Validate Listing business rules.
     */
    @Transactional(readOnly = true)
    public ValidationResultResponse validateListing(String listingId) {
        ListingEntity listing = getListingOrThrow(listingId);
        return validationService.validate(listing);
    }

    /**
     * MARKET-004: Publish Listing (DRAFT -> ACTIVE).
     */
    @Transactional
    public ListingResponse publishListing(String listingId, SecurityPrincipal principal) {
        ListingEntity listing = getListingOrThrow(listingId);
        assertOwnershipOrAdmin(listing, principal);

        ValidationResultResponse validation = validationService.validate(listing);
        if (!validation.isValid()) {
            throw new BusinessException("Listing validation failed: " + String.join(", ", validation.getErrors()));
        }

        listing.publish();
        ListingEntity published = listingRepository.save(listing);
        recordOutboxEvent("ListingPublished", published);

        log.info("Published listing: id={}, farmerId={}, availableQty={}", published.getId(), principal.getUserId(), published.getAvailableQuantity());
        return toListingResponse(published, principal);
    }

    /**
     * MARKET-005: Edit Listing with state and business rule checks.
     */
    @Transactional
    public ListingResponse editListing(String listingId, UpdateListingRequest request, SecurityPrincipal principal) {
        ListingEntity listing = getListingOrThrow(listingId);
        assertOwnershipOrAdmin(listing, principal);

        if (listing.getStatus().isTerminal()) {
            throw new BusinessException("Cannot edit a listing in terminal status: " + listing.getStatus());
        }

        if (listing.getStatus() == ListingStatus.DRAFT) {
            // Full edit allowed in DRAFT
            if (request.getTitle() != null && !request.getTitle().isBlank()) {
                listing.setTitle(request.getTitle().trim());
            }
            if (request.getDescription() != null) {
                listing.setDescription(request.getDescription());
            }
            if (request.getTotalQuantity() != null) {
                if (request.getTotalQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("Quantity must be greater than zero");
                }
                listing.setTotalQuantity(request.getTotalQuantity());
                listing.setAvailableQuantity(request.getTotalQuantity());
            }
            if (request.getPricePerUnit() != null) {
                if (request.getPricePerUnit().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("Price per unit must be greater than zero");
                }
                listing.setPricePerUnit(request.getPricePerUnit());
            }
            if (request.getQualityGrade() != null) {
                listing.setQualityGrade(request.getQualityGrade());
            }
            if (request.getHarvestDate() != null) {
                listing.setHarvestDate(request.getHarvestDate());
            }
            if (request.getMinOrderQuantity() != null) {
                listing.setMinOrderQuantity(request.getMinOrderQuantity());
            }
            if (request.getLocation() != null) {
                updateLocation(listing, request.getLocation());
            }
        } else if (listing.getStatus() == ListingStatus.ACTIVE) {
            // Restricted edit for ACTIVE listing
            if (request.getTitle() != null && !request.getTitle().isBlank()) {
                listing.setTitle(request.getTitle().trim());
            }
            if (request.getDescription() != null) {
                listing.setDescription(request.getDescription());
            }
            if (request.getPricePerUnit() != null) {
                if (request.getPricePerUnit().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("Price per unit must be greater than zero");
                }
                listing.setPricePerUnit(request.getPricePerUnit());
            }
            if (request.getTotalQuantity() != null) {
                // Cannot reduce total quantity below reserved amount
                BigDecimal committed = listing.getTotalQuantity().subtract(listing.getAvailableQuantity());
                if (request.getTotalQuantity().compareTo(committed) < 0) {
                    throw new BusinessException("Cannot reduce total quantity below currently committed/reserved quantity (" + committed + ")");
                }
                BigDecimal diff = request.getTotalQuantity().subtract(listing.getTotalQuantity());
                listing.setTotalQuantity(request.getTotalQuantity());
                listing.setAvailableQuantity(listing.getAvailableQuantity().add(diff));
            }
            if (request.getLocation() != null) {
                updateLocation(listing, request.getLocation());
            }
            if (request.getHarvestDate() != null) {
                listing.setHarvestDate(request.getHarvestDate());
            }
            if (request.getMinOrderQuantity() != null) {
                listing.setMinOrderQuantity(request.getMinOrderQuantity());
            }
        }

        ListingEntity updated = listingRepository.save(listing);
        recordOutboxEvent("ListingUpdated", updated);

        log.info("Updated listing: id={}, status={}", updated.getId(), updated.getStatus());
        return toListingResponse(updated, principal);
    }

    /**
     * MARKET-006: Cancel an active or draft listing safely.
     */
    @Transactional
    public ListingResponse cancelListing(String listingId, CancelListingRequest request, SecurityPrincipal principal) {
        ListingEntity listing = getListingOrThrow(listingId);
        assertOwnershipOrAdmin(listing, principal);

        String reason = (request != null && request.getReason() != null) ? request.getReason() : "Cancelled by farmer";
        listing.cancel(reason);

        ListingEntity cancelled = listingRepository.save(listing);
        recordOutboxEvent("ListingCancelled", cancelled);

        log.info("Cancelled listing: id={}, farmerId={}, reason={}", cancelled.getId(), principal.getUserId(), reason);
        return toListingResponse(cancelled, principal);
    }

    /**
     * MARKET-007: Add image reference to listing (Object storage metadata).
     */
    @Transactional
    public ListingImageResponse addImage(String listingId, AddImageRequest request, SecurityPrincipal principal) {
        ListingEntity listing = getListingOrThrow(listingId);
        assertOwnershipOrAdmin(listing, principal);

        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            listingImageRepository.resetPrimaryFlagsForListing(listingId);
        }

        ListingImageEntity imageEntity = ListingImageEntity.builder()
                .id(UUID.randomUUID().toString())
                .listing(listing)
                .imageUrl(request.getImageUrl().trim())
                .storageKey(request.getStorageKey())
                .isPrimary(Boolean.TRUE.equals(request.getIsPrimary()))
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : listing.getImages().size() + 1)
                .build();

        ListingImageEntity saved = listingImageRepository.save(imageEntity);
        log.info("Added image reference to listing: listingId={}, imageId={}", listingId, saved.getId());

        return toImageResponse(saved);
    }

    /**
     * MARKET-007: Remove image reference from listing.
     */
    @Transactional
    public void removeImage(String listingId, String imageId, SecurityPrincipal principal) {
        ListingEntity listing = getListingOrThrow(listingId);
        assertOwnershipOrAdmin(listing, principal);

        listingImageRepository.deleteByListingIdAndId(listingId, imageId);
        log.info("Removed image reference: listingId={}, imageId={}", listingId, imageId);
    }

    /**
     * MARKET-007: Set an existing image as primary.
     */
    @Transactional
    public void setPrimaryImage(String listingId, String imageId, SecurityPrincipal principal) {
        ListingEntity listing = getListingOrThrow(listingId);
        assertOwnershipOrAdmin(listing, principal);

        listingImageRepository.resetPrimaryFlagsForListing(listingId);
        ListingImageEntity image = listingImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with ID: " + imageId));

        image.setIsPrimary(true);
        listingImageRepository.save(image);
        log.info("Set primary image: listingId={}, imageId={}", listingId, imageId);
    }

    @Transactional(readOnly = true)
    public ListingResponse getListingById(String listingId, SecurityPrincipal principal) {
        ListingEntity listing = getListingOrThrow(listingId);
        return toListingResponse(listing, principal);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getFarmerListings(SecurityPrincipal principal, ListingStatus status) {
        List<ListingEntity> listings;
        if (status != null) {
            listings = listingRepository.findByFarmerIdAndStatusOrderByCreatedAtDesc(principal.getUserId(), status);
        } else {
            listings = listingRepository.findByFarmerIdOrderByCreatedAtDesc(principal.getUserId());
        }

        return listings.stream()
                .map(l -> toListingResponse(l, principal))
                .toList();
    }

    /**
     * DISC-001, DISC-002, DISC-003, DISC-004:
     * Paginated marketplace discovery with keyword search, multi-attribute filtering, and sorting.
     */
    @Transactional(readOnly = true)
    public PageResponse<ListingSummaryResponse> searchListings(ListingSearchCriteria criteria) {
        int page = criteria.getPage() >= 0 ? criteria.getPage() : 0;
        int size = criteria.getSize() > 0 ? Math.min(criteria.getSize(), 100) : 20;

        Sort sort;
        String sortBy = criteria.getSortBy() != null ? criteria.getSortBy().toUpperCase() : "NEWEST";
        switch (sortBy) {
            case "PRICE_ASC" -> sort = Sort.by(Sort.Direction.ASC, "pricePerUnit");
            case "PRICE_DESC" -> sort = Sort.by(Sort.Direction.DESC, "pricePerUnit");
            case "HARVEST_DATE_ASC" -> sort = Sort.by(Sort.Direction.ASC, "harvestDate");
            case "HARVEST_DATE_DESC" -> sort = Sort.by(Sort.Direction.DESC, "harvestDate");
            default -> sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ListingEntity> entityPage = listingRepository.findAll(ListingSpecification.build(criteria), pageable);

        Page<ListingSummaryResponse> summaryPage = entityPage.map(this::toSummaryResponse);
        return PageResponse.from(summaryPage);
    }

    @Transactional(readOnly = true)
    public List<ListingSummaryResponse> getActiveListings() {
        return listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE)
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    /**
     * ORDER-003 & ORDER-004: Atomically reserve quantity and protect against overselling.
     */
    @Transactional
    public StockOperationResponse reserveStock(String listingId, ReserveStockRequest request) {
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Reserved quantity must be greater than zero");
        }

        ListingEntity listing = getListingOrThrow(listingId);
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new BusinessException("Listing is not available for orders (status: " + listing.getStatus() + ")");
        }

        if (listing.getMinOrderQuantity() != null && request.getQuantity().compareTo(listing.getMinOrderQuantity()) < 0) {
            throw new BusinessException("Requested quantity " + request.getQuantity() + " is below minimum order quantity of " + listing.getMinOrderQuantity());
        }

        int rows = listingRepository.reserveQuantityAtomically(listingId, request.getQuantity());
        if (rows == 0) {
            // Re-fetch to produce detailed exception message
            ListingEntity current = getListingOrThrow(listingId);
            throw new BusinessException("Insufficient stock available: requested " + request.getQuantity() +
                    " " + current.getUnit() + ", but only " + current.getAvailableQuantity() + " " + current.getUnit() + " remaining.");
        }

        // Re-read updated entity state
        ListingEntity updated = getListingOrThrow(listingId);
        if (updated.getAvailableQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            updated.setStatus(ListingStatus.SOLD_OUT);
            listingRepository.save(updated);
            log.info("Listing {} is now SOLD_OUT", listingId);
        }

        recordOutboxEvent("StockReserved", updated);

        return StockOperationResponse.builder()
                .listingId(listingId)
                .reservedQuantity(request.getQuantity())
                .remainingAvailableQuantity(updated.getAvailableQuantity())
                .status(updated.getStatus())
                .success(true)
                .message("Stock reserved successfully")
                .build();
    }

    /**
     * ORDER-007: Release reserved quantity back into available stock when order is rejected/cancelled.
     */
    @Transactional
    public StockOperationResponse releaseStock(String listingId, ReserveStockRequest request) {
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Released quantity must be greater than zero");
        }

        int rows = listingRepository.releaseQuantityAtomically(listingId, request.getQuantity());
        if (rows == 0) {
            throw new BusinessException("Failed to release stock: invalid reserved quantity or listing not found.");
        }

        ListingEntity updated = getListingOrThrow(listingId);
        if (updated.getStatus() == ListingStatus.SOLD_OUT && updated.getAvailableQuantity().compareTo(BigDecimal.ZERO) > 0) {
            updated.setStatus(ListingStatus.ACTIVE);
            listingRepository.save(updated);
            log.info("Listing {} status restored from SOLD_OUT to ACTIVE", listingId);
        }

        recordOutboxEvent("StockReleased", updated);

        return StockOperationResponse.builder()
                .listingId(listingId)
                .reservedQuantity(request.getQuantity())
                .remainingAvailableQuantity(updated.getAvailableQuantity())
                .status(updated.getStatus())
                .success(true)
                .message("Stock released successfully")
                .build();
    }

    // Helper methods

    private ListingEntity getListingOrThrow(String id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with ID: " + id));
    }

    private void assertOwnershipOrAdmin(ListingEntity listing, SecurityPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required.");
        }
        if (principal.getRole() == UserRole.ADMIN) {
            return;
        }
        if (!listing.getFarmerId().equals(principal.getUserId())) {
            throw new AccessDeniedException("You are not authorized to modify this listing.");
        }
    }

    private void updateLocation(ListingEntity listing, ListingLocationDto loc) {
        if (loc.getLatitude() != null) listing.setLatitude(loc.getLatitude());
        if (loc.getLongitude() != null) listing.setLongitude(loc.getLongitude());
        if (loc.getLocality() != null && !loc.getLocality().isBlank()) listing.setLocality(loc.getLocality().trim());
        if (loc.getDistrict() != null) listing.setDistrict(loc.getDistrict().trim());
        if (loc.getVisibility() != null) listing.setLocationVisibility(loc.getVisibility());
    }

    private void recordOutboxEvent(String eventType, ListingEntity listing) {
        try {
            Map<String, Object> payload = Map.of(
                    "listingId", listing.getId(),
                    "farmerId", listing.getFarmerId(),
                    "produceCode", listing.getProduce().getCode(),
                    "status", listing.getStatus().name(),
                    "availableQuantity", listing.getAvailableQuantity(),
                    "pricePerUnit", listing.getPricePerUnit(),
                    "timestamp", Instant.now().toString()
            );

            MarketplaceOutboxEntity outbox = MarketplaceOutboxEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("LISTING")
                    .aggregateId(listing.getId())
                    .eventType(eventType)
                    .payload(objectMapper.writeValueAsString(payload))
                    .status("PENDING")
                    .build();

            outboxRepository.save(outbox);
        } catch (Exception e) {
            log.error("Failed to write outbox event: {}", e.getMessage());
        }
    }

    public ListingResponse toListingResponse(ListingEntity entity, SecurityPrincipal principal) {
        ProduceResponse produceResponse = produceService.toResponse(entity.getProduce());

        boolean isOwnerOrAdmin = principal != null && (principal.getRole() == UserRole.ADMIN || principal.getUserId().equals(entity.getFarmerId()));

        // Section 5 Location Privacy:
        // Approximate location is displayed to buyers by default
        BigDecimal lat = entity.getLatitude();
        BigDecimal lon = entity.getLongitude();
        if (!isOwnerOrAdmin && entity.getLocationVisibility() == LocationVisibility.APPROXIMATE) {
            // Fuzz coordinates to ~1-2km resolution for privacy protection
            lat = lat.setScale(2, java.math.RoundingMode.HALF_UP);
            lon = lon.setScale(2, java.math.RoundingMode.HALF_UP);
        }

        ListingLocationDto locationDto = ListingLocationDto.builder()
                .latitude(lat)
                .longitude(lon)
                .locality(entity.getLocality())
                .district(entity.getDistrict())
                .visibility(entity.getLocationVisibility())
                .build();

        List<ListingImageResponse> imageResponses = entity.getImages().stream()
                .map(this::toImageResponse)
                .toList();

        SellerSummaryDto sellerSummary = authServiceClient.getSellerPublicProfile(entity.getFarmerId());

        return ListingResponse.builder()
                .id(entity.getId())
                .farmerId(entity.getFarmerId())
                .seller(sellerSummary)
                .produce(produceResponse)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .totalQuantity(entity.getTotalQuantity())
                .availableQuantity(entity.getAvailableQuantity())
                .reservedQuantity(entity.getReservedQuantity())
                .unit(entity.getUnit())
                .pricePerUnit(entity.getPricePerUnit())
                .qualityGrade(entity.getQualityGrade())
                .harvestDate(entity.getHarvestDate())
                .status(entity.getStatus())
                .minOrderQuantity(entity.getMinOrderQuantity())
                .location(locationDto)
                .cancellationReason(entity.getCancellationReason())
                .publishedAt(entity.getPublishedAt())
                .cancelledAt(entity.getCancelledAt())
                .images(imageResponses)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ListingSummaryResponse toSummaryResponse(ListingEntity entity) {
        String primaryImage = entity.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(ListingImageEntity::getImageUrl)
                .findFirst()
                .orElse(entity.getImages().isEmpty() ? null : entity.getImages().get(0).getImageUrl());

        return ListingSummaryResponse.builder()
                .id(entity.getId())
                .farmerId(entity.getFarmerId())
                .produceName(entity.getProduce().getName())
                .produceCode(entity.getProduce().getCode())
                .title(entity.getTitle())
                .availableQuantity(entity.getAvailableQuantity())
                .unit(entity.getUnit())
                .pricePerUnit(entity.getPricePerUnit())
                .qualityGrade(entity.getQualityGrade())
                .harvestDate(entity.getHarvestDate())
                .status(entity.getStatus())
                .locality(entity.getLocality())
                .district(entity.getDistrict())
                .primaryImageUrl(primaryImage)
                .build();
    }

    private ListingImageResponse toImageResponse(ListingImageEntity img) {
        return ListingImageResponse.builder()
                .id(img.getId())
                .imageUrl(img.getImageUrl())
                .storageKey(img.getStorageKey())
                .isPrimary(img.getIsPrimary())
                .displayOrder(img.getDisplayOrder())
                .createdAt(img.getCreatedAt())
                .build();
    }
}
