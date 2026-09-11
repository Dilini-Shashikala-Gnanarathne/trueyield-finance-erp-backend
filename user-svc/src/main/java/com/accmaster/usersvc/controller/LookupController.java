package com.accmaster.usersvc.controller;

import com.accmaster.usersvc.domain.entity.BatchEntity;
import com.accmaster.usersvc.domain.entity.CityEntity;
import com.accmaster.usersvc.domain.entity.DistrictEntity;
import com.accmaster.usersvc.dto.response.ApiResponse;
import com.accmaster.usersvc.service.LookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookups")
@RequiredArgsConstructor
@Tag(name = "Lookup Data", description = "Endpoints for retrieving reference batches, districts, and cities")
public class LookupController {

    private final LookupService lookupService;

    @GetMapping("/batches")
    @Operation(summary = "Get active academic batches")
    public ResponseEntity<ApiResponse<List<BatchEntity>>> getActiveBatches() {
        List<BatchEntity> batches = lookupService.getActiveBatches();
        return ResponseEntity.ok(ApiResponse.ok("Active batches retrieved.", batches));
    }

    @GetMapping("/districts")
    @Operation(summary = "Get all administrative districts")
    public ResponseEntity<ApiResponse<List<DistrictEntity>>> getAllDistricts() {
        List<DistrictEntity> districts = lookupService.getAllDistricts();
        return ResponseEntity.ok(ApiResponse.ok("Districts retrieved.", districts));
    }

    @GetMapping("/districts/{districtId}/cities")
    @Operation(summary = "Get cities within a district")
    public ResponseEntity<ApiResponse<List<CityEntity>>> getCitiesByDistrict(@PathVariable Integer districtId) {
        List<CityEntity> cities = lookupService.getCitiesByDistrict(districtId);
        return ResponseEntity.ok(ApiResponse.ok("Cities retrieved.", cities));
    }
}
