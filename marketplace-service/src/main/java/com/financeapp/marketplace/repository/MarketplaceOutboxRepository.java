package com.financeapp.marketplace.repository;

import com.financeapp.marketplace.domain.entity.MarketplaceOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketplaceOutboxRepository extends JpaRepository<MarketplaceOutboxEntity, String> {

    List<MarketplaceOutboxEntity> findByStatusOrderByCreatedAtAsc(String status);
}
