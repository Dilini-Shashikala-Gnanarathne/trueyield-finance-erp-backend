package com.financeapp.order.controller;

import com.financeapp.order.domain.enums.OrderStatus;
import com.financeapp.order.dto.ApiResponse;
import com.financeapp.order.dto.PageResponse;
import com.financeapp.order.dto.order.*;
import com.financeapp.order.security.SecurityPrincipal;
import com.financeapp.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Lifecycle & Management", description = "Endpoints for buyer orders, stock reservation, idempotency, and farmer order actions (ORDER-001 to ORDER-009, PRICE-001 to PRICE-003)")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Create order with idempotent stock reservation (ORDER-001, ORDER-002, ORDER-003, ORDER-004, ORDER-005)")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        OrderResponse response = orderService.createOrder(request, idempotencyKey, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order created successfully in PENDING state.", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details by ID with access control")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable String id,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        OrderResponse response = orderService.getOrderById(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Order retrieved successfully.", response));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    @Operation(summary = "Farmer accepts an incoming order: PENDING -> ACCEPTED (ORDER-006)")
    public ResponseEntity<ApiResponse<OrderResponse>> acceptOrder(
            @PathVariable String id,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        OrderResponse response = orderService.acceptOrder(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Order accepted successfully.", response));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    @Operation(summary = "Farmer rejects an incoming order: PENDING -> REJECTED with stock rollback (ORDER-007)")
    public ResponseEntity<ApiResponse<OrderResponse>> rejectOrder(
            @PathVariable String id,
            @Valid @RequestBody RejectOrderRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        OrderResponse response = orderService.rejectOrder(id, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Order rejected and reserved stock released.", response));
    }

    @GetMapping("/buyer/me")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Buyer order history with pagination and optional status filter (ORDER-008)")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getBuyerOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        PageResponse<OrderSummaryResponse> response = orderService.getBuyerOrders(principal, status, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Buyer order history retrieved.", response));
    }

    @GetMapping("/farmer/me")
    @PreAuthorize("hasRole('FARMER')")
    @Operation(summary = "Farmer incoming order management with pagination and optional status filter (ORDER-009)")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getFarmerOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        PageResponse<OrderSummaryResponse> response = orderService.getFarmerOrders(principal, status, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Farmer incoming orders retrieved.", response));
    }

    @PostMapping("/calculate-price")
    @Operation(summary = "Preview price breakdown calculation before placing order (PRICE-002, PRICE-003)")
    public ResponseEntity<ApiResponse<PriceBreakdownDto>> calculatePrice(
            @Valid @RequestBody CalculatePriceRequest request) {
        PriceBreakdownDto response = orderService.previewPriceBreakdown(request);
        return ResponseEntity.ok(ApiResponse.ok("Price breakdown calculated.", response));
    }
}
