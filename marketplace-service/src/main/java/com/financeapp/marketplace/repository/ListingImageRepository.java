package com.financeapp.marketplace.repository;

import com.financeapp.marketplace.domain.entity.ListingImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingImageRepository extends JpaRepository<ListingImageEntity, String> {

    List<ListingImageEntity> findByListingIdOrderByDisplayOrderAsc(String listingId);

    @Modifying
    @Query("UPDATE ListingImageEntity img SET img.isPrimary = false WHERE img.listing.id = :listingId")
    void resetPrimaryFlagsForListing(@Param("listingId") String listingId);

    void deleteByListingIdAndId(String listingId, String id);
}
