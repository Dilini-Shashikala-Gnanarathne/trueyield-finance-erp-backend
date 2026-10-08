package com.financeapp.order.service;

import com.financeapp.order.domain.enums.OrderStatus;
import com.financeapp.order.dto.dashboard.*;
import com.financeapp.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "farmerDashboard", key = "#farmerId")
    public FarmerDashboardDto getFarmerDashboard(String farmerId) {
        long pending   = orderRepository.countByFarmerIdAndStatus(farmerId, OrderStatus.PENDING);
        long accepted  = orderRepository.countByFarmerIdAndStatus(farmerId, OrderStatus.ACCEPTED);
        long paid      = orderRepository.countByFarmerIdAndStatus(farmerId, OrderStatus.PAID);
        long fulfilled = orderRepository.countByFarmerIdAndStatus(farmerId, OrderStatus.FULFILLED);
        long completed = orderRepository.countByFarmerIdAndStatus(farmerId, OrderStatus.COMPLETED);
        long rejected  = orderRepository.countByFarmerIdAndStatus(farmerId, OrderStatus.REJECTED);
        long cancelled = orderRepository.countByFarmerIdAndStatus(farmerId, OrderStatus.CANCELLED);

        // Revenue = PAID + FULFILLED + COMPLETED orders (committed spend)
        BigDecimal salesCompleted  = orderRepository.sumTotalSalesByFarmerIdAndStatus(farmerId, OrderStatus.COMPLETED);
        BigDecimal salesFulfilled  = orderRepository.sumTotalSalesByFarmerIdAndStatus(farmerId, OrderStatus.FULFILLED);
        BigDecimal salesPaid       = orderRepository.sumTotalSalesByFarmerIdAndStatus(farmerId, OrderStatus.PAID);
        BigDecimal totalSales      = salesCompleted.add(salesFulfilled).add(salesPaid);

        return FarmerDashboardDto.builder()
                .farmerId(farmerId)
                .pendingOrders(pending)
                .acceptedOrders(accepted)
                .paidOrders(paid)
                .fulfilledOrders(fulfilled)
                .completedOrders(completed)
                .rejectedOrders(rejected)
                .cancelledOrders(cancelled)
                .totalSales(totalSales)
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "buyerDashboard", key = "#buyerId")
    public BuyerDashboardDto getBuyerDashboard(String buyerId) {
        long pending   = orderRepository.countByBuyerIdAndStatus(buyerId, OrderStatus.PENDING);
        long accepted  = orderRepository.countByBuyerIdAndStatus(buyerId, OrderStatus.ACCEPTED);
        long paid      = orderRepository.countByBuyerIdAndStatus(buyerId, OrderStatus.PAID);
        long fulfilled = orderRepository.countByBuyerIdAndStatus(buyerId, OrderStatus.FULFILLED);
        long completed = orderRepository.countByBuyerIdAndStatus(buyerId, OrderStatus.COMPLETED);
        long cancelled = orderRepository.countByBuyerIdAndStatus(buyerId, OrderStatus.CANCELLED);

        // Total spent = PAID + FULFILLED + COMPLETED
        BigDecimal spentCompleted = orderRepository.sumTotalSpentByBuyerIdAndStatus(buyerId, OrderStatus.COMPLETED);
        BigDecimal spentFulfilled = orderRepository.sumTotalSpentByBuyerIdAndStatus(buyerId, OrderStatus.FULFILLED);
        BigDecimal spentPaid      = orderRepository.sumTotalSpentByBuyerIdAndStatus(buyerId, OrderStatus.PAID);
        BigDecimal totalSpent     = spentCompleted.add(spentFulfilled).add(spentPaid);

        return BuyerDashboardDto.builder()
                .buyerId(buyerId)
                .pendingOrders(pending)
                .acceptedOrders(accepted)
                .paidOrders(paid)
                .fulfilledOrders(fulfilled)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .totalSpent(totalSpent)
                .build();
    }

    @Transactional(readOnly = true)
    public SalesSummaryDto getFarmerSalesSummary(String farmerId) {
        BigDecimal salesCompleted = orderRepository.sumTotalSalesByFarmerIdAndStatus(farmerId, OrderStatus.COMPLETED);
        BigDecimal salesFulfilled = orderRepository.sumTotalSalesByFarmerIdAndStatus(farmerId, OrderStatus.FULFILLED);
        BigDecimal salesPaid      = orderRepository.sumTotalSalesByFarmerIdAndStatus(farmerId, OrderStatus.PAID);
        BigDecimal totalSales     = salesCompleted.add(salesFulfilled).add(salesPaid);

        java.util.List<ProduceSalesDto> salesByProduce =
                orderRepository.findSalesByProduceForFarmer(farmerId, OrderStatus.COMPLETED);

        return SalesSummaryDto.builder()
                .farmerId(farmerId)
                .totalOverallRevenue(totalSales)
                .salesByProduce(salesByProduce)
                .build();
    }

    @Transactional(readOnly = true)
    public OrderStatsDto getAdminOrderStats() {
        long total     = orderRepository.count();
        long pending   = orderRepository.countByStatus(OrderStatus.PENDING);
        long accepted  = orderRepository.countByStatus(OrderStatus.ACCEPTED);
        long paid      = orderRepository.countByStatus(OrderStatus.PAID);
        long fulfilled = orderRepository.countByStatus(OrderStatus.FULFILLED);
        long completed = orderRepository.countByStatus(OrderStatus.COMPLETED);
        long rejected  = orderRepository.countByStatus(OrderStatus.REJECTED);
        long cancelled = orderRepository.countByStatus(OrderStatus.CANCELLED);

        BigDecimal revCompleted = orderRepository.sumTotalAmountByStatus(OrderStatus.COMPLETED);
        BigDecimal revFulfilled = orderRepository.sumTotalAmountByStatus(OrderStatus.FULFILLED);
        BigDecimal revPaid      = orderRepository.sumTotalAmountByStatus(OrderStatus.PAID);
        BigDecimal totalRevenue = revCompleted.add(revFulfilled).add(revPaid);

        return OrderStatsDto.builder()
                .totalOrders(total)
                .pendingOrders(pending)
                .acceptedOrders(accepted)
                .paidOrders(paid)
                .fulfilledOrders(fulfilled)
                .completedOrders(completed)
                .rejectedOrders(rejected)
                .cancelledOrders(cancelled)
                .totalPlatformRevenue(totalRevenue)
                .build();
    }
}

