package com.financeapp.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.order.client.MarketplaceClient;
import com.financeapp.order.domain.entity.OrderEntity;
import com.financeapp.order.domain.entity.OrderOutboxEntity;
import com.financeapp.order.domain.enums.OrderStatus;
import com.financeapp.order.domain.enums.UserRole;
import com.financeapp.order.dto.PageResponse;
import com.financeapp.order.dto.order.*;
import com.financeapp.order.exception.BusinessException;
import com.financeapp.order.exception.ResourceNotFoundException;
import com.financeapp.order.repository.OrderOutboxRepository;
import com.financeapp.order.repository.OrderRepository;
import com.financeapp.order.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderOutboxRepository outboxRepository;
    private final MarketplaceClient marketplaceClient;
    private final PricingService pricingService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    /**
     * ORDER-001, ORDER-002, ORDER-003, ORDER-004, ORDER-005:
     * Idempotent, concurrency-safe buyer order creation with availability validation & stock reservation.
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String idempotencyKey, SecurityPrincipal principal) {
        if (principal == null || principal.getRole() != UserRole.BUYER) {
            throw new AccessDeniedException("Only registered buyers can place orders.");
        }

        // ORDER-005: Check idempotency cache
        Optional<OrderResponse> cached = idempotencyService.checkOrReserveKey(idempotencyKey, principal.getUserId(), request);
        if (cached.isPresent()) {
            return cached.get();
        }

        String orderId = UUID.randomUUID().toString();
        try {
            // ORDER-002: Fetch & validate listing availability
            MarketplaceClient.ListingInfo listing = marketplaceClient.getListing(request.getListingId());

            if (!"ACTIVE".equalsIgnoreCase(listing.getStatus())) {
                throw new BusinessException("Listing is not currently active for purchase (status: " + listing.getStatus() + ")");
            }

            if (principal.getUserId().equals(listing.getFarmerId())) {
                throw new BusinessException("Farmers cannot buy from their own produce listings.");
            }

            if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Order quantity must be greater than zero.");
            }

            if (listing.getMinOrderQuantity() != null && request.getQuantity().compareTo(listing.getMinOrderQuantity()) < 0) {
                throw new BusinessException("Requested quantity " + request.getQuantity() + " is less than minimum order quantity of " + listing.getMinOrderQuantity() + " " + listing.getUnit());
            }

            if (request.getQuantity().compareTo(listing.getAvailableQuantity()) > 0) {
                throw new BusinessException("Requested quantity " + request.getQuantity() + " exceeds currently available stock of " + listing.getAvailableQuantity() + " " + listing.getUnit());
            }

            // ORDER-003 & ORDER-004: Atomically reserve stock in marketplace-service
            marketplaceClient.reserveStock(request.getListingId(), request.getQuantity(), orderId);

            // PRICE-001, PRICE-002, PRICE-003: Calculate order totals
            PriceBreakdownDto pricing = pricingService.calculatePrice(
                    request.getQuantity(),
                    listing.getPricePerUnit(),
                    listing.getUnit(),
                    request.isIncludeDelivery()
            );

            String orderNumber = "ORD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            OrderEntity order = OrderEntity.builder()
                    .id(orderId)
                    .orderNumber(orderNumber)
                    .buyerId(principal.getUserId())
                    .farmerId(listing.getFarmerId())
                    .listingId(listing.getListingId())
                    .produceName(listing.getProduceName())
                    .quantity(request.getQuantity())
                    .unit(listing.getUnit())
                    .unitPrice(listing.getPricePerUnit())
                    .subtotal(pricing.getSubtotal())
                    .platformFee(pricing.getPlatformFee())
                    .deliveryFee(pricing.getDeliveryFee())
                    .totalAmount(pricing.getTotalAmount())
                    .currency(pricing.getCurrency())
                    .status(OrderStatus.PENDING)
                    .deliveryAddress(request.getDeliveryAddress())
                    .buyerNotes(request.getBuyerNotes())
                    .build();

            OrderEntity saved = orderRepository.save(order);
            recordOutboxEvent("OrderCreated", saved);

            OrderResponse response = toOrderResponse(saved, pricing);

            // ORDER-005: Cache completed response for idempotent replay
            idempotencyService.markCompleted(idempotencyKey, principal.getUserId(), orderId, response);

            log.info("Order created successfully: id={}, orderNumber={}, buyerId={}, farmerId={}, total={}",
                    saved.getId(), saved.getOrderNumber(), saved.getBuyerId(), saved.getFarmerId(), saved.getTotalAmount());

            return response;

        } catch (Exception e) {
            idempotencyService.markFailed(idempotencyKey, principal.getUserId());
            log.error("Failed to create order for listingId={}: {}", request.getListingId(), e.getMessage());
            throw e;
        }
    }

    /**
     * ORDER-006: Farmer Accept Order (PENDING -> ACCEPTED).
     */
    @Transactional
    public OrderResponse acceptOrder(String orderId, SecurityPrincipal principal) {
        OrderEntity order = getOrderOrThrow(orderId);
        assertFarmerOwnership(order, principal);

        order.accept();
        OrderEntity updated = orderRepository.save(order);
        recordOutboxEvent("OrderAccepted", updated);

        log.info("Order accepted by farmer: orderId={}, farmerId={}", orderId, principal.getUserId());
        return toOrderResponse(updated);
    }

    /**
     * ORDER-007: Farmer Reject Order (PENDING -> REJECTED) with stock rollback.
     */
    @Transactional
    public OrderResponse rejectOrder(String orderId, RejectOrderRequest request, SecurityPrincipal principal) {
        OrderEntity order = getOrderOrThrow(orderId);
        assertFarmerOwnership(order, principal);

        order.reject(request.getReason());
        OrderEntity updated = orderRepository.save(order);

        // Release reserved stock back into available stock
        marketplaceClient.releaseStock(order.getListingId(), order.getQuantity(), order.getId());

        recordOutboxEvent("OrderRejected", updated);

        log.info("Order rejected by farmer: orderId={}, farmerId={}, reason={}", orderId, principal.getUserId(), request.getReason());
        return toOrderResponse(updated);
    }

    /**
     * ORDER-008: Buyer Order History (paginated).
     */
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getBuyerOrders(SecurityPrincipal principal, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<OrderEntity> orderPage;

        if (status != null) {
            orderPage = orderRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(principal.getUserId(), status, pageable);
        } else {
            orderPage = orderRepository.findByBuyerIdOrderByCreatedAtDesc(principal.getUserId(), pageable);
        }

        return PageResponse.from(orderPage.map(this::toOrderSummaryResponse));
    }

    /**
     * ORDER-009: Farmer Order Management (paginated).
     */
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getFarmerOrders(SecurityPrincipal principal, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<OrderEntity> orderPage;

        if (status != null) {
            orderPage = orderRepository.findByFarmerIdAndStatusOrderByCreatedAtDesc(principal.getUserId(), status, pageable);
        } else {
            orderPage = orderRepository.findByFarmerIdOrderByCreatedAtDesc(principal.getUserId(), pageable);
        }

        return PageResponse.from(orderPage.map(this::toOrderSummaryResponse));
    }

    /**
     * Get Order details by ID with authorization verification.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId, SecurityPrincipal principal) {
        OrderEntity order = getOrderOrThrow(orderId);

        boolean isBuyer = principal.getUserId().equals(order.getBuyerId());
        boolean isFarmer = principal.getUserId().equals(order.getFarmerId());
        boolean isAdmin = principal.getRole() == UserRole.ADMIN;

        if (!isBuyer && !isFarmer && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to view this order.");
        }

        return toOrderResponse(order);
    }

    /**
     * PRICE-002, PRICE-003: Price Breakdown preview calculation.
     */
    @Transactional(readOnly = true)
    public PriceBreakdownDto previewPriceBreakdown(CalculatePriceRequest request) {
        MarketplaceClient.ListingInfo listing = marketplaceClient.getListing(request.getListingId());
        return pricingService.calculatePrice(
                request.getQuantity(),
                listing.getPricePerUnit(),
                listing.getUnit(),
                request.isIncludeDelivery()
        );
    }

    private OrderEntity getOrderOrThrow(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
    }

    private void assertFarmerOwnership(OrderEntity order, SecurityPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required.");
        }
        if (principal.getRole() == UserRole.ADMIN) {
            return;
        }
        if (principal.getRole() != UserRole.FARMER || !order.getFarmerId().equals(principal.getUserId())) {
            throw new AccessDeniedException("Only the farmer assigned to this order can perform this action.");
        }
    }

    private void recordOutboxEvent(String eventType, OrderEntity order) {
        try {
            Map<String, Object> payload = Map.of(
                    "orderId", order.getId(),
                    "orderNumber", order.getOrderNumber(),
                    "buyerId", order.getBuyerId(),
                    "farmerId", order.getFarmerId(),
                    "listingId", order.getListingId(),
                    "quantity", order.getQuantity(),
                    "totalAmount", order.getTotalAmount(),
                    "status", order.getStatus().name(),
                    "timestamp", Instant.now().toString()
            );

            OrderOutboxEntity outbox = OrderOutboxEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("ORDER")
                    .aggregateId(order.getId())
                    .eventType(eventType)
                    .payload(objectMapper.writeValueAsString(payload))
                    .status("PENDING")
                    .build();

            outboxRepository.save(outbox);
        } catch (Exception e) {
            log.error("Failed to write order outbox event: {}", e.getMessage());
        }
    }

    private OrderResponse toOrderResponse(OrderEntity order) {
        PriceBreakdownDto breakdown = pricingService.calculatePrice(
                order.getQuantity(),
                order.getUnitPrice(),
                order.getUnit(),
                order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0
        );
        return toOrderResponse(order, breakdown);
    }

    private OrderResponse toOrderResponse(OrderEntity order, PriceBreakdownDto breakdown) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .buyerId(order.getBuyerId())
                .farmerId(order.getFarmerId())
                .listingId(order.getListingId())
                .produceName(order.getProduceName())
                .quantity(order.getQuantity())
                .unit(order.getUnit())
                .unitPrice(order.getUnitPrice())
                .subtotal(order.getSubtotal())
                .platformFee(order.getPlatformFee())
                .deliveryFee(order.getDeliveryFee())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .status(order.getStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .buyerNotes(order.getBuyerNotes())
                .rejectionReason(order.getRejectionReason())
                .priceBreakdown(breakdown)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderSummaryResponse toOrderSummaryResponse(OrderEntity order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .listingId(order.getListingId())
                .produceName(order.getProduceName())
                .quantity(order.getQuantity())
                .unit(order.getUnit())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
