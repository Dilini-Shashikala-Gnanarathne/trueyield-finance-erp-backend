package com.accmaster.usersvc.repository.spec;

import com.accmaster.usersvc.domain.entity.StudentEntity;
import com.accmaster.usersvc.domain.entity.UserEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring Data JPA Specification for dynamic multi-field student filtering.
 * Replaces the PHP FilterQueryBuilder pattern with a type-safe, composable approach.
 *
 * Fields supported (matching PHP filterStudents):
 *   academicId, name (partial), mobile, email, nic, whatsappNumber, username,
 *   gender, status, batchId, school, guardianName, guardianMobile,
 *   registeredAtFrom, registeredAtTo
 */
public class StudentSpecification {

    public record FilterParams(
            String academicId,
            String name,
            String mobile,
            String email,
            String nic,
            String whatsappNumber,
            String username,
            String gender,
            String status,
            String batchId,
            String school,
            String guardianName,
            String guardianMobile,
            Instant registeredAtFrom,
            Instant registeredAtTo
    ) {}

    public static Specification<StudentEntity> withFilters(FilterParams f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join usr_user
            Join<StudentEntity, UserEntity> user = root.join("user");

            // Soft-delete guard (belt-and-suspenders; @SQLRestriction handles this on UserEntity)
            predicates.add(cb.isNull(user.get("deletedAt")));

            // Academic ID — exact match
            if (f.academicId() != null && !f.academicId().isBlank()) {
                predicates.add(cb.equal(root.get("academicId"), f.academicId()));
            }

            // Name — partial match on fname OR lname (matches PHP OR group)
            if (f.name() != null && !f.name().isBlank()) {
                String pattern = "%" + f.name().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fname")), pattern),
                        cb.like(cb.lower(root.get("lname")), pattern)
                ));
            }

            // Mobile — exact match on usr_user.mobile
            if (f.mobile() != null && !f.mobile().isBlank()) {
                predicates.add(cb.equal(user.get("mobile"), f.mobile()));
            }

            // Email — exact match
            if (f.email() != null && !f.email().isBlank()) {
                predicates.add(cb.equal(user.get("email"), f.email()));
            }

            // NIC — exact match
            if (f.nic() != null && !f.nic().isBlank()) {
                predicates.add(cb.equal(user.get("nic"), f.nic()));
            }

            // WhatsApp number
            if (f.whatsappNumber() != null && !f.whatsappNumber().isBlank()) {
                predicates.add(cb.equal(root.get("whatsappNumber"), f.whatsappNumber()));
            }

            // Username (= mobile in most cases)
            if (f.username() != null && !f.username().isBlank()) {
                predicates.add(cb.equal(user.get("username"), f.username()));
            }

            // Gender
            if (f.gender() != null && !f.gender().isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("gender").as(String.class)), f.gender().toUpperCase()));
            }

            // Status
            if (f.status() != null && !f.status().isBlank()) {
                predicates.add(cb.equal(cb.upper(user.get("status").as(String.class)), f.status().toUpperCase()));
            }

            // Batch ID — exact match
            if (f.batchId() != null && !f.batchId().isBlank()) {
                predicates.add(cb.equal(root.get("batch").get("id"), f.batchId()));
            }

            // School — partial match
            if (f.school() != null && !f.school().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("school")), "%" + f.school().toLowerCase() + "%"));
            }

            // Guardian name — partial match
            if (f.guardianName() != null && !f.guardianName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("guardianName")), "%" + f.guardianName().toLowerCase() + "%"));
            }

            // Guardian mobile — exact
            if (f.guardianMobile() != null && !f.guardianMobile().isBlank()) {
                predicates.add(cb.equal(root.get("guardianMobile"), f.guardianMobile()));
            }

            // Date range: registeredAt
            if (f.registeredAtFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(user.get("registeredAt"), f.registeredAtFrom()));
            }
            if (f.registeredAtTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(user.get("registeredAt"), f.registeredAtTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
