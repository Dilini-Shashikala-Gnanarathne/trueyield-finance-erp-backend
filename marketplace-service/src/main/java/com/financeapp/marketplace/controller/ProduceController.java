package com.financeapp.marketplace.controller;

import com.financeapp.marketplace.dto.ApiResponse;
import com.financeapp.marketplace.dto.produce.CreateProduceRequest;
import com.financeapp.marketplace.dto.produce.ProduceResponse;
import com.financeapp.marketplace.service.ProduceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/produce")
@RequiredArgsConstructor
@Tag(name = "Produce Catalogue", description = "Endpoints for managing produce types (MARKET-001)")
public class ProduceController {

    private final ProduceService produceService;

    @GetMapping
    @Operation(summary = "Get all active produce types in catalogue (Rambutan initial)")
    public ResponseEntity<ApiResponse<List<ProduceResponse>>> getAllActiveProduce() {
        List<ProduceResponse> produceList = produceService.getAllActiveProduce();
        return ResponseEntity.ok(ApiResponse.ok("Produce catalogue retrieved.", produceList));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get produce type details by ID")
    public ResponseEntity<ApiResponse<ProduceResponse>> getProduceById(@PathVariable String id) {
        ProduceResponse produce = produceService.getProduceById(id);
        return ResponseEntity.ok(ApiResponse.ok("Produce retrieved.", produce));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get produce type details by code (e.g. RAMBUTAN)")
    public ResponseEntity<ApiResponse<ProduceResponse>> getProduceByCode(@PathVariable String code) {
        ProduceResponse produce = produceService.getProduceByCode(code);
        return ResponseEntity.ok(ApiResponse.ok("Produce retrieved.", produce));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new produce catalogue type (Admin only)")
    public ResponseEntity<ApiResponse<ProduceResponse>> createProduce(@Valid @RequestBody CreateProduceRequest request) {
        ProduceResponse created = produceService.createProduce(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Produce created successfully.", created));
    }
}
