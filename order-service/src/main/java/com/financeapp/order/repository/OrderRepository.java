package com.financeapp.order.repository;

import com.financeapp.order.domain.entity.OrderEntity;
import com.financeapp.order.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    Page<OrderEntity> findByBuyerIdOrderByCreatedAtDesc(String buyerId, Pageable pageable);

    Page<OrderEntity> findByBuyerIdAndStatusOrderByCreatedAtDesc(String buyerId, OrderStatus status, Pageable pageable);

    Page<OrderEntity> findByFarmerIdOrderByCreatedAtDesc(String farmerId, Pageable pageable);

    Page<OrderEntity> findByFarmerIdAndStatusOrderByCreatedAtDesc(String farmerId, OrderStatus status, Pageable pageable);

    long countByFarmerIdAndStatus(String farmerId, OrderStatus status);

    long countByBuyerIdAndStatus(String buyerId, OrderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o WHERE o.farmerId = :farmerId AND o.status = :status")
    java.math.BigDecimal sumTotalSalesByFarmerIdAndStatus(@org.springframework.data.repository.query.Param("farmerId") String farmerId, @org.springframework.data.repository.query.Param("status") OrderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o WHERE o.buyerId = :buyerId AND o.status = :status")
    java.math.BigDecimal sumTotalSpentByBuyerIdAndStatus(@org.springframework.data.repository.query.Param("buyerId") String buyerId, @org.springframework.data.repository.query.Param("status") OrderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT new com.financeapp.order.dto.dashboard.ProduceSalesDto(o.produceName, SUM(o.quantity), SUM(o.totalAmount)) FROM OrderEntity o WHERE o.farmerId = :farmerId AND o.status = :status GROUP BY o.produceName")
    java.util.List<com.financeapp.order.dto.dashboard.ProduceSalesDto> findSalesByProduceForFarmer(@org.springframework.data.repository.query.Param("farmerId") String farmerId, @org.springframework.data.repository.query.Param("status") OrderStatus status);

    long countByStatus(OrderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o WHERE o.status = :status")
    java.math.BigDecimal sumTotalAmountByStatus(@org.springframework.data.repository.query.Param("status") OrderStatus status);
}
