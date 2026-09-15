package com.financeapp.marketplace.client;

import com.financeapp.marketplace.dto.listing.SellerSummaryDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;
    private final String authServiceUrl;
    private final ObjectMapper objectMapper;

    public AuthServiceClient(
            RestTemplateBuilder builder,
            @Value("${auth.service.url:http://localhost:8085}") String authServiceUrl,
            ObjectMapper objectMapper) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
        this.authServiceUrl = authServiceUrl;
        this.objectMapper = objectMapper;
    }

    public SellerSummaryDto getSellerPublicProfile(String sellerId) {
        try {
            String url = authServiceUrl + "/api/v1/profile/seller/" + sellerId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode data = root.get("data");
                if (data != null && !data.isNull()) {
                    return SellerSummaryDto.builder()
                            .sellerId(sellerId)
                            .sellerName(data.hasNonNull("sellerName") ? data.get("sellerName").asText() : "Local Farmer")
                            .farmName(data.hasNonNull("farmName") ? data.get("farmName").asText() : null)
                            .locality(data.hasNonNull("locality") ? data.get("locality").asText() : null)
                            .district(data.hasNonNull("district") ? data.get("district").asText() : null)
                            .avatarUrl(data.hasNonNull("avatarUrl") ? data.get("avatarUrl").asText() : null)
                            .build();
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch seller public profile for sellerId={}: {}", sellerId, e.getMessage());
        }

        // Graceful fallback for seller info (DISC-005)
        return SellerSummaryDto.builder()
                .sellerId(sellerId)
                .sellerName("Verified Farmer")
                .build();
    }
}
