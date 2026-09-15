package com.financeapp.marketplace.repository;

import com.financeapp.marketplace.domain.entity.ListingEntity;
import com.financeapp.marketplace.domain.enums.ListingStatus;
import com.financeapp.marketplace.dto.listing.ListingSearchCriteria;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ListingSpecification {

    public static Specification<ListingEntity> build(ListingSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always enforce ACTIVE status for discovery
            predicates.add(cb.equal(root.get("status"), ListingStatus.ACTIVE));

            if (criteria == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // DISC-002: Keyword search across title, description, produce name, produce code
            if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
                String searchPattern = "%" + criteria.getQuery().trim().toLowerCase() + "%";
                Join<Object, Object> produceJoin = root.join("produce");

                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                Predicate produceNameMatch = cb.like(cb.lower(produceJoin.get("name")), searchPattern);
                Predicate produceCodeMatch = cb.like(cb.lower(produceJoin.get("code")), searchPattern);

                predicates.add(cb.or(titleMatch, descMatch, produceNameMatch, produceCodeMatch));
            }

            // DISC-003: Filter by produce ID
            if (criteria.getProduceId() != null && !criteria.getProduceId().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("produce").get("id"), criteria.getProduceId().trim()));
            }

            // DISC-003: Filter by produce code
            if (criteria.getProduceCode() != null && !criteria.getProduceCode().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("produce").get("code"), criteria.getProduceCode().trim()));
            }

            // DISC-003: Filter by produce category
            if (criteria.getCategory() != null) {
                predicates.add(cb.equal(root.get("produce").get("category"), criteria.getCategory()));
            }

            // DISC-003: Filter by locality
            if (criteria.getLocality() != null && !criteria.getLocality().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("locality")), "%" + criteria.getLocality().trim().toLowerCase() + "%"));
            }

            // DISC-003: Filter by district
            if (criteria.getDistrict() != null && !criteria.getDistrict().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("district")), "%" + criteria.getDistrict().trim().toLowerCase() + "%"));
            }

            // DISC-003: Filter by min price
            if (criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerUnit"), criteria.getMinPrice()));
            }

            // DISC-003: Filter by max price
            if (criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerUnit"), criteria.getMaxPrice()));
            }

            // DISC-003: Filter by quality grade
            if (criteria.getQualityGrade() != null) {
                predicates.add(cb.equal(root.get("qualityGrade"), criteria.getQualityGrade()));
            }

            // DISC-003: Filter by stock availability
            if (Boolean.TRUE.equals(criteria.getInStockOnly())) {
                predicates.add(cb.greaterThan(root.get("availableQuantity"), BigDecimal.ZERO));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
