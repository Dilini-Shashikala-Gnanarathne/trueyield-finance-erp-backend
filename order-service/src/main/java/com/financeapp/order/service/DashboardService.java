package com.financeapp.order.service;

import com.financeapp.order.domain.enums.OrderStatus;
import com.financeapp.order.dto.dashboard.BuyerDashboardDto;
import com.financeapp.order.dto.dashboard.FarmerDashboardDto;
import com.financeapp.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public FarmerDashboardDto getFarmerDashboard(String farmerId) {
        long pending = orderRepository.countByFarmerIdAndStatus(farmerId, OrderStatus.PENDING);
        long completed = orderRepository.countByFarmerIdAndStatus(farmerId, OrderStatus.COMPLETED);
        long rejected = orderRepository.countByFarmerIdAndStatus(farmerId, OrderStatus.REJECTED);
        java.math.BigDecimal totalSales = orderRepository.sumTotalSalesByFarmerIdAndStatus(farmerId, OrderStatus.COMPLETED);

        return FarmerDashboardDto.builder()
                .farmerId(farmerId)
                .pendingOrders(pending)
                .completedOrders(completed)
                .rejectedOrders(rejected)
                .totalSales(totalSales)
                .build();
    }

    @Transactional(readOnly = true)
    public BuyerDashboardDto getBuyerDashboard(String buyerId) {
        long pending = orderRepository.countByBuyerIdAndStatus(buyerId, OrderStatus.PENDING);
        long fulfilled = orderRepository.countByBuyerIdAndStatus(buyerId, OrderStatus.FULFILLED);
        long completed = orderRepository.countByBuyerIdAndStatus(buyerId, OrderStatus.COMPLETED);
        java.math.BigDecimal totalSpent = orderRepository.sumTotalSpentByBuyerIdAndStatus(buyerId, OrderStatus.COMPLETED);

        return BuyerDashboardDto.builder()
                .buyerId(buyerId)
                .pendingOrders(pending)
                .fulfilledOrders(fulfilled)
                .completedOrders(completed)
                .totalSpent(totalSpent)
                .build();
    }

    @Transactional(readOnly = true)
    public com.financeapp.order.dto.dashboard.SalesSummaryDto getFarmerSalesSummary(String farmerId) {
        java.math.BigDecimal totalSales = orderRepository.sumTotalSalesByFarmerIdAndStatus(farmerId, OrderStatus.COMPLETED);
        java.util.List<com.financeapp.order.dto.dashboard.ProduceSalesDto> salesByProduce = 
            orderRepository.findSalesByProduceForFarmer(farmerId, OrderStatus.COMPLETED);

        return com.financeapp.order.dto.dashboard.SalesSummaryDto.builder()
                .farmerId(farmerId)
                .totalOverallRevenue(totalSales)
                .salesByProduce(salesByProduce)
                .build();
    }

    @Transactional(readOnly = true)
    public com.financeapp.order.dto.dashboard.OrderStatsDto getAdminOrderStats() {
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long completedOrders = orderRepository.countByStatus(OrderStatus.COMPLETED);
        java.math.BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatus(OrderStatus.COMPLETED);

        return com.financeapp.order.dto.dashboard.OrderStatsDto.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .totalPlatformRevenue(totalRevenue)
                .build();
    }
}
