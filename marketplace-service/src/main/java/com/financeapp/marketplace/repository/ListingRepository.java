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

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM ListingEntity l WHERE l.id = :id")
    java.util.Optional<ListingEntity> findByIdForUpdate(@Param("id") String id);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE ListingEntity l SET l.availableQuantity = l.availableQuantity - :qty, " +
           "l.reservedQuantity = l.reservedQuantity + :qty, " +
           "l.version = l.version + 1 " +
           "WHERE l.id = :id AND l.availableQuantity >= :qty AND l.status = 'ACTIVE'")
    int reserveQuantityAtomically(@Param("id") String id, @Param("qty") java.math.BigDecimal qty);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE ListingEntity l SET l.availableQuantity = l.availableQuantity + :qty, " +
           "l.reservedQuantity = l.reservedQuantity - :qty, " +
           "l.version = l.version + 1 " +
           "WHERE l.id = :id AND l.reservedQuantity >= :qty")
    int releaseQuantityAtomically(@Param("id") String id, @Param("qty") java.math.BigDecimal qty);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE ListingEntity l SET l.status = :newStatus WHERE l.id = :id")
    int updateStatus(@Param("id") String id, @Param("newStatus") ListingStatus newStatus);
}

