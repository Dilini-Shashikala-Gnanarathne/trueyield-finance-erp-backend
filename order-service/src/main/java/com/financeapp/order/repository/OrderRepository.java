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
}
