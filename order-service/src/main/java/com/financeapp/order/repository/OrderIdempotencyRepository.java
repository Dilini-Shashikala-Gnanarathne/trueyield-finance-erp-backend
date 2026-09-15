package com.financeapp.order.repository;

import com.financeapp.order.domain.entity.OrderIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderIdempotencyRepository extends JpaRepository<OrderIdempotencyEntity, String> {

    Optional<OrderIdempotencyEntity> findByIdempotencyKeyAndBuyerId(String idempotencyKey, String buyerId);
}
