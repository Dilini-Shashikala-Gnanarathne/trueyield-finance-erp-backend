package com.financeapp.marketplace.grpc;

import com.financeapp.marketplace.domain.entity.ListingEntity;
import com.financeapp.marketplace.domain.enums.ListingStatus;
import com.financeapp.marketplace.exception.BusinessException;
import com.financeapp.marketplace.exception.ResourceNotFoundException;
import com.financeapp.marketplace.grpc.proto.*;
import com.financeapp.marketplace.repository.ListingRepository;
import com.financeapp.marketplace.service.ListingService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import com.financeapp.marketplace.dto.listing.ReserveStockRequest;
import com.financeapp.marketplace.dto.listing.StockOperationResponse;

import java.math.BigDecimal;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class MarketplaceGrpcService extends MarketplaceServiceGrpc.MarketplaceServiceImplBase {

    private final ListingService listingService;
    private final ListingRepository listingRepository;

    @Override
    public void getListing(GetListingRequest request, StreamObserver<GetListingResponse> responseObserver) {
        try {
            ListingEntity listing = listingRepository.findById(request.getListingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

            GetListingResponse response = GetListingResponse.newBuilder()
                    .setListingId(listing.getId())
                    .setFarmerId(listing.getFarmerId())
                    .setProduceName(listing.getProduce().getName())
                    .setTitle(listing.getTitle())
                    .setStatus(listing.getStatus().name())
                    .setAvailableQuantity(listing.getAvailableQuantity().toString())
                    .setMinOrderQuantity(listing.getMinOrderQuantity() != null ? listing.getMinOrderQuantity().toString() : "")
                    .setUnit(listing.getUnit())
                    .setPricePerUnit(listing.getPricePerUnit().toString())
                    .setCurrency("LKR")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ResourceNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC getListing failed", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void reserveStock(com.financeapp.marketplace.grpc.proto.ReserveStockRequest request, StreamObserver<com.financeapp.marketplace.grpc.proto.ReserveStockResponse> responseObserver) {
        try {
            ReserveStockRequest reserveReq = new ReserveStockRequest();
            reserveReq.setQuantity(new BigDecimal(request.getQuantity()));
            
            StockOperationResponse opRes = listingService.reserveStock(request.getListingId(), reserveReq);
            
            com.financeapp.marketplace.grpc.proto.ReserveStockResponse response = com.financeapp.marketplace.grpc.proto.ReserveStockResponse.newBuilder()
                    .setSuccess(opRes.isSuccess())
                    .setMessage(opRes.getMessage())
                    .setRemainingAvailableQuantity(opRes.getRemainingAvailableQuantity().toString())
                    .setListingStatus(opRes.getStatus().name())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (BusinessException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC reserveStock failed", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void releaseStock(com.financeapp.marketplace.grpc.proto.ReleaseStockRequest request, StreamObserver<com.financeapp.marketplace.grpc.proto.ReleaseStockResponse> responseObserver) {
        try {
            ReserveStockRequest releaseReq = new ReserveStockRequest();
            releaseReq.setQuantity(new BigDecimal(request.getQuantity()));
            
            StockOperationResponse opRes = listingService.releaseStock(request.getListingId(), releaseReq);
            
            com.financeapp.marketplace.grpc.proto.ReleaseStockResponse response = com.financeapp.marketplace.grpc.proto.ReleaseStockResponse.newBuilder()
                    .setSuccess(opRes.isSuccess())
                    .setMessage(opRes.getMessage())
                    .setListingStatus(opRes.getStatus().name())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (BusinessException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC releaseStock failed", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void checkHealth(MarketplaceHealthRequest request, StreamObserver<MarketplaceHealthResponse> responseObserver) {
        MarketplaceHealthResponse response = MarketplaceHealthResponse.newBuilder()
                .setStatus("UP")
                .setVersion("1.0.0")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
