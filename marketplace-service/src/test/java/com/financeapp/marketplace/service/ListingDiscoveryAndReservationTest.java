package com.financeapp.marketplace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.marketplace.client.AuthServiceClient;
import com.financeapp.marketplace.domain.entity.ListingEntity;
import com.financeapp.marketplace.domain.entity.ProduceEntity;
import com.financeapp.marketplace.domain.enums.ListingStatus;
import com.financeapp.marketplace.domain.enums.LocationVisibility;
import com.financeapp.marketplace.domain.enums.QualityGrade;
import com.financeapp.marketplace.domain.enums.UserRole;
import com.financeapp.marketplace.dto.PageResponse;
import com.financeapp.marketplace.dto.listing.*;
import com.financeapp.marketplace.dto.produce.ProduceResponse;
import com.financeapp.marketplace.exception.BusinessException;
import com.financeapp.marketplace.repository.ListingImageRepository;
import com.financeapp.marketplace.repository.ListingRepository;
import com.financeapp.marketplace.repository.MarketplaceOutboxRepository;
import com.financeapp.marketplace.repository.ProduceRepository;
import com.financeapp.marketplace.security.SecurityPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingDiscoveryAndReservationTest {

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private ProduceRepository produceRepository;
    @Mock
    private ListingImageRepository listingImageRepository;
    @Mock
    private MarketplaceOutboxRepository outboxRepository;
    @Mock
    private ListingValidationService validationService;
    @Mock
    private ProduceService produceService;
    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ListingService listingService;

    private ListingEntity testListing;
    private ProduceEntity testProduce;

    @BeforeEach
    void setUp() {
        testProduce = ProduceEntity.builder()
                .id("prod-rambutan-01")
                .code("RAMBUTAN_MALWANA")
                .name("Malwana Rambutan")
                .defaultUnit("KG")
                .supportedUnits("KG")
                .supportedGrades("PREMIUM,STANDARD,PROCESSING")
                .category(com.financeapp.marketplace.domain.enums.ProduceCategory.FRUIT)
                .isActive(true)
                .build();

        testListing = ListingEntity.builder()
                .id("list-001")
                .farmerId("farmer-123")
                .produce(testProduce)
                .title("Sweet Malwana Rambutan Direct from Tree")
                .description("Harvested today, grade A fresh rambutan.")
                .totalQuantity(new BigDecimal("100.00"))
                .availableQuantity(new BigDecimal("100.00"))
                .reservedQuantity(BigDecimal.ZERO)
                .unit("KG")
                .pricePerUnit(new BigDecimal("450.00"))
                .qualityGrade(QualityGrade.PREMIUM)
                .harvestDate(LocalDate.now())
                .status(ListingStatus.ACTIVE)
                .minOrderQuantity(new BigDecimal("5.00"))
                .latitude(new BigDecimal("6.9270790"))
                .longitude(new BigDecimal("79.8612440"))
                .locality("Malwana")
                .district("Gampaha")
                .locationVisibility(LocationVisibility.APPROXIMATE)
                .build();
    }

    @Test
    @DisplayName("DISC-001 & DISC-004: Browse active listings returns paginated result with correct sorting")
    void testSearchListingsPaginated() {
        ListingSearchCriteria criteria = ListingSearchCriteria.builder()
                .page(0)
                .size(10)
                .sortBy("PRICE_ASC")
                .build();

        when(listingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testListing)));

        PageResponse<ListingSummaryResponse> response = listingService.searchListings(criteria);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        ListingSummaryResponse item = response.getContent().get(0);
        assertThat(item.getId()).isEqualTo("list-001");
        assertThat(item.getProduceName()).isEqualTo("Malwana Rambutan");
        assertThat(item.getPricePerUnit()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("DISC-005: Get listing details enriches with seller public profile and fuzzes approximate location")
    void testGetListingDetailsWithSellerInfoAndLocationFuzzing() {
        when(listingRepository.findById("list-001")).thenReturn(Optional.of(testListing));
        when(produceService.toResponse(any())).thenReturn(
                ProduceResponse.builder().id("prod-rambutan-01").name("Malwana Rambutan").code("RAMBUTAN_MALWANA").build()
        );
        when(authServiceClient.getSellerPublicProfile("farmer-123")).thenReturn(
                SellerSummaryDto.builder()
                        .sellerId("farmer-123")
                        .sellerName("Nimal Bandara")
                        .farmName("Malwana Orchard")
                        .locality("Malwana")
                        .district("Gampaha")
                        .build()
        );

        SecurityPrincipal buyerPrincipal = new SecurityPrincipal("buyer-999", "Buyer Name", "+94770000000", "buyer@test.com", UserRole.BUYER);

        ListingResponse response = listingService.getListingById("list-001", buyerPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("list-001");
        assertThat(response.getSeller()).isNotNull();
        assertThat(response.getSeller().getSellerName()).isEqualTo("Nimal Bandara");
        assertThat(response.getSeller().getFarmName()).isEqualTo("Malwana Orchard");

        // Coordinates should be fuzzed to 2 decimal places for privacy (Section 5)
        assertThat(response.getLocation().getLatitude()).isEqualByComparingTo(new BigDecimal("6.93"));
        assertThat(response.getLocation().getLongitude()).isEqualByComparingTo(new BigDecimal("79.86"));
        assertThat(response.getLocation().getLocality()).isEqualTo("Malwana");
    }

    @Test
    @DisplayName("ORDER-003 & ORDER-004: Reserve stock succeeds atomically when quantity is available")
    void testReserveStockSuccess() {
        when(listingRepository.findById("list-001")).thenReturn(Optional.of(testListing));
        when(listingRepository.reserveQuantityAtomically(eq("list-001"), eq(new BigDecimal("20.00")))).thenReturn(1);

        ListingEntity updatedListing = ListingEntity.builder()
                .id("list-001")
                .farmerId("farmer-123")
                .produce(testProduce)
                .title("Sweet Malwana Rambutan Direct from Tree")
                .availableQuantity(new BigDecimal("80.00"))
                .reservedQuantity(new BigDecimal("20.00"))
                .status(ListingStatus.ACTIVE)
                .build();
        when(listingRepository.findById("list-001")).thenReturn(Optional.of(testListing), Optional.of(updatedListing));

        ReserveStockRequest request = ReserveStockRequest.builder()
                .quantity(new BigDecimal("20.00"))
                .orderId("ord-123")
                .build();

        StockOperationResponse result = listingService.reserveStock("list-001", request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getReservedQuantity()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.getRemainingAvailableQuantity()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(result.getStatus()).isEqualTo(ListingStatus.ACTIVE);
    }

    @Test
    @DisplayName("ORDER-004: Prevent overselling throws BusinessException when stock is insufficient")
    void testReserveStockFailsWhenInsufficientStock() {
        testListing.setAvailableQuantity(new BigDecimal("10.00"));
        when(listingRepository.findById("list-001")).thenReturn(Optional.of(testListing));
        when(listingRepository.reserveQuantityAtomically(eq("list-001"), eq(new BigDecimal("50.00")))).thenReturn(0);

        ReserveStockRequest request = ReserveStockRequest.builder()
                .quantity(new BigDecimal("50.00"))
                .orderId("ord-124")
                .build();

        assertThatThrownBy(() -> listingService.reserveStock("list-001", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock available");
    }

    @Test
    @DisplayName("ORDER-007: Release stock restores available quantity atomically")
    void testReleaseStockSuccess() {
        when(listingRepository.releaseQuantityAtomically(eq("list-001"), eq(new BigDecimal("20.00")))).thenReturn(1);

        ListingEntity updatedListing = ListingEntity.builder()
                .id("list-001")
                .farmerId("farmer-123")
                .produce(testProduce)
                .title("Sweet Malwana Rambutan Direct from Tree")
                .availableQuantity(new BigDecimal("100.00"))
                .reservedQuantity(BigDecimal.ZERO)
                .status(ListingStatus.ACTIVE)
                .build();
        when(listingRepository.findById("list-001")).thenReturn(Optional.of(updatedListing));

        ReserveStockRequest request = ReserveStockRequest.builder()
                .quantity(new BigDecimal("20.00"))
                .orderId("ord-123")
                .build();

        StockOperationResponse result = listingService.releaseStock("list-001", request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getRemainingAvailableQuantity()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
