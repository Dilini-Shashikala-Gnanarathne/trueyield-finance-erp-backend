package com.financeapp.auth.repository;

import com.financeapp.auth.domain.entity.UserEntity;
import com.financeapp.auth.domain.enums.UserRole;
import com.financeapp.auth.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByPhone(String phone);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE (u.phone = :identifier OR (u.email IS NOT NULL AND LOWER(u.email) = LOWER(:identifier))) AND u.deletedAt IS NULL")
    Optional<UserEntity> findByIdentifier(@Param("identifier") String identifier);

    long countByRole(UserRole role);

    /**
     * Admin paginated user list with optional role and status filters.
     * Passing null for a filter parameter disables that filter (matches all).
     */
    @Query("SELECT u FROM UserEntity u WHERE u.deletedAt IS NULL " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "ORDER BY u.registeredAt DESC")
    Page<UserEntity> findAllByFilters(
            @Param("role")   UserRole   role,
            @Param("status") UserStatus status,
            Pageable pageable);
}
