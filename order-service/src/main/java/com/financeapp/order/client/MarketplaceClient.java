package com.financeapp.order.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeapp.order.exception.BusinessException;
import com.financeapp.order.exception.ResourceNotFoundException;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Component
@Slf4j
public class MarketplaceClient {

    private final RestTemplate restTemplate;
    private final String marketplaceUrl;
    private final ObjectMapper objectMapper;

    public MarketplaceClient(
            RestTemplateBuilder builder,
            @Value("${marketplace.service.url:http://localhost:8086}") String marketplaceUrl,
            ObjectMapper objectMapper) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
        this.marketplaceUrl = marketplaceUrl;
        this.objectMapper = objectMapper;
    }

    public ListingInfo getListing(String listingId) {
        try {
            String url = marketplaceUrl + "/api/v1/listings/" + listingId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");
            if (data == null || data.isNull()) {
                throw new ResourceNotFoundException("Listing not found with ID: " + listingId);
            }

            String farmerId = data.get("farmerId").asText();
            String status = data.get("status").asText();
            BigDecimal availableQuantity = new BigDecimal(data.get("availableQuantity").asText());
            BigDecimal minOrderQuantity = data.hasNonNull("minOrderQuantity")
                    ? new BigDecimal(data.get("minOrderQuantity").asText())
                    : null;
            String unit = data.get("unit").asText();
            BigDecimal pricePerUnit = new BigDecimal(data.get("pricePerUnit").asText());
            String produceName = data.get("produce").get("name").asText();
            String title = data.get("title").asText();

            return ListingInfo.builder()
                    .listingId(listingId)
                    .farmerId(farmerId)
                    .produceName(produceName)
                    .title(title)
                    .status(status)
                    .availableQuantity(availableQuantity)
                    .minOrderQuantity(minOrderQuantity)
                    .unit(unit)
                    .pricePerUnit(pricePerUnit)
                    .build();

        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Listing not found with ID: " + listingId);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch listing {}: {}", listingId, e.getMessage());
            throw new BusinessException("Could not verify listing availability with Marketplace Service: " + e.getMessage());
        }
    }

    public void reserveStock(String listingId, BigDecimal quantity, String orderId) {
        try {
            String url = marketplaceUrl + "/api/v1/listings/" + listingId + "/reserve";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "quantity", quantity,
                    "orderId", orderId
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException("Failed to reserve stock in Marketplace Service.");
            }
        } catch (HttpClientErrorException e) {
            String errorMsg = extractErrorMessage(e.getResponseBodyAsString(), "Failed to reserve stock: " + e.getMessage());
            throw new BusinessException(errorMsg);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Stock reservation failed for listing {}: {}", listingId, e.getMessage());
            throw new BusinessException("Stock reservation failed: " + e.getMessage());
        }
    }

    public void releaseStock(String listingId, BigDecimal quantity, String orderId) {
        try {
            String url = marketplaceUrl + "/api/v1/listings/" + listingId + "/release";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "quantity", quantity,
                    "orderId", orderId
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);
            log.info("Released stock for listingId={}, quantity={}, orderId={}", listingId, quantity, orderId);
        } catch (Exception e) {
            log.error("Failed to release stock for listing {}: {}", listingId, e.getMessage());
        }
    }

    private String extractErrorMessage(String body, String defaultMsg) {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.hasNonNull("message")) {
                return root.get("message").asText();
            }
        } catch (Exception ignored) {}
        return defaultMsg;
    }

    @Data
    @Builder
    public static class ListingInfo {
        private String listingId;
        private String farmerId;
        private String produceName;
        private String title;
        private String status;
        private BigDecimal availableQuantity;
        private BigDecimal minOrderQuantity;
        private String unit;
        private BigDecimal pricePerUnit;
    }
}
