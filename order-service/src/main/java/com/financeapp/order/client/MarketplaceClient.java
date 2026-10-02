package com.financeapp.order.client;

import com.financeapp.marketplace.grpc.proto.*;
import com.financeapp.order.exception.BusinessException;
import com.financeapp.order.exception.ResourceNotFoundException;
import io.grpc.StatusRuntimeException;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class MarketplaceClient {

    @GrpcClient("marketplace-service")
    private MarketplaceServiceGrpc.MarketplaceServiceBlockingStub marketplaceStub;

    public ListingInfo getListing(String listingId) {
        try {
            GetListingResponse response = marketplaceStub.getListing(
                    GetListingRequest.newBuilder().setListingId(listingId).build());

            return ListingInfo.builder()
                    .listingId(response.getListingId())
                    .farmerId(response.getFarmerId())
                    .produceName(response.getProduceName())
                    .title(response.getTitle())
                    .status(response.getStatus())
                    .availableQuantity(new BigDecimal(response.getAvailableQuantity()))
                    .minOrderQuantity(response.getMinOrderQuantity().isEmpty() ? null : new BigDecimal(response.getMinOrderQuantity()))
                    .unit(response.getUnit())
                    .pricePerUnit(new BigDecimal(response.getPricePerUnit()))
                    .build();
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                throw new ResourceNotFoundException("Listing not found with ID: " + listingId);
            }
            log.error("gRPC getListing failed for {}: {}", listingId, e.getStatus());
            throw new BusinessException("Could not verify listing availability with Marketplace Service: " + e.getStatus().getDescription());
        }
    }

    public void reserveStock(String listingId, BigDecimal quantity, String orderId) {
        try {
            ReserveStockResponse response = marketplaceStub.reserveStock(
                    ReserveStockRequest.newBuilder()
                            .setListingId(listingId)
                            .setQuantity(quantity.toString())
                            .setOrderId(orderId)
                            .build());

            if (!response.getSuccess()) {
                throw new BusinessException(response.getMessage());
            }
        } catch (StatusRuntimeException e) {
            log.error("gRPC reserveStock failed for {}: {}", listingId, e.getStatus());
            throw new BusinessException("Failed to reserve stock: " + e.getStatus().getDescription());
        }
    }

    public void releaseStock(String listingId, BigDecimal quantity, String orderId) {
        try {
            ReleaseStockResponse response = marketplaceStub.releaseStock(
                    ReleaseStockRequest.newBuilder()
                            .setListingId(listingId)
                            .setQuantity(quantity.toString())
                            .setOrderId(orderId)
                            .build());

            log.info("Released stock for listingId={}, quantity={}, orderId={}", listingId, quantity, orderId);
        } catch (StatusRuntimeException e) {
            log.error("Failed to release stock for listing {}: {}", listingId, e.getStatus());
        }
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
