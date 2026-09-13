package com.financeapp.marketplace.repository;

import com.financeapp.marketplace.domain.entity.ListingEntity;
import com.financeapp.marketplace.domain.enums.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<ListingEntity, String>, JpaSpecificationExecutor<ListingEntity> {

    List<ListingEntity> findByFarmerIdOrderByCreatedAtDesc(String farmerId);

    List<ListingEntity> findByFarmerIdAndStatusOrderByCreatedAtDesc(String farmerId, ListingStatus status);

    List<ListingEntity> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    @Query("SELECT l FROM ListingEntity l WHERE l.status = :status AND l.produce.id = :produceId")
    List<ListingEntity> findByProduceIdAndStatus(@Param("produceId") String produceId, @Param("status") ListingStatus status);
}
