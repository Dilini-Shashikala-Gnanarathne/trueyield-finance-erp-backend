package com.financeapp.order.repository;

import com.financeapp.order.domain.entity.OrderOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderOutboxRepository extends JpaRepository<OrderOutboxEntity, String> {

    List<OrderOutboxEntity> findTop50ByStatusOrderByCreatedAtAsc(String status);
}
