package com.financeapp.auth.repository;

import com.financeapp.auth.domain.entity.UserEntity;
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
}
