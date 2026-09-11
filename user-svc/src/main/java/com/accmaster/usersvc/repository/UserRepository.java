package com.accmaster.usersvc.repository;

import com.accmaster.usersvc.domain.entity.UserEntity;
import com.accmaster.usersvc.domain.enums.UserStatus;
import com.accmaster.usersvc.domain.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

/**
 * User repository with JpaSpecificationExecutor for dynamic filtering.
 * All queries automatically exclude soft-deleted records via @SQLRestriction on UserEntity.
 */
public interface UserRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByMobile(String mobile);

    Optional<UserEntity> findByNic(String nic);

    boolean existsByMobile(String mobile);

    boolean existsByNic(String nic);

    boolean existsByUsername(String username);

    // Check uniqueness while excluding a specific user (for identifier update)
    boolean existsByMobileAndIdNot(String mobile, String id);

    boolean existsByNicAndIdNot(String nic, String id);

    // Find by mobile or NIC — used for duplicate check during registration
    @Query("""
        SELECT u FROM UserEntity u
        WHERE (u.mobile = :mobile OR (:nic IS NOT NULL AND u.nic = :nic))
    """)
    Optional<UserEntity> findByMobileOrNic(@Param("mobile") String mobile, @Param("nic") String nic);

    // Force password reset flag
    @Modifying
    @Query("UPDATE UserEntity u SET u.forcePasswordReset = :flag, u.updatedAt = :now, u.updatedBy = :updatedBy WHERE u.id = :id")
    int setForcePasswordReset(@Param("id") String id,
                               @Param("flag") boolean flag,
                               @Param("now") Instant now,
                               @Param("updatedBy") String updatedBy);

    // Status update
    @Modifying
    @Query("UPDATE UserEntity u SET u.status = :status, u.updatedAt = :now, u.updatedBy = :updatedBy WHERE u.id = :id")
    int updateStatus(@Param("id") String id,
                     @Param("status") UserStatus status,
                     @Param("now") Instant now,
                     @Param("updatedBy") String updatedBy);

    // Password update with audit trail
    @Modifying
    @Query("UPDATE UserEntity u SET u.password = :password, u.lastPasswordResetAt = :now, u.updatedAt = :now, u.updatedBy = :updatedBy, u.forcePasswordReset = false WHERE u.id = :id")
    int updatePassword(@Param("id") String id,
                       @Param("password") String password,
                       @Param("now") Instant now,
                       @Param("updatedBy") String updatedBy);

    // Username sync (used after identifier update)
    @Modifying
    @Query("UPDATE UserEntity u SET u.username = :username, u.updatedAt = :now, u.updatedBy = :updatedBy WHERE u.id = :id")
    int updateUsername(@Param("id") String id,
                       @Param("username") String username,
                       @Param("now") Instant now,
                       @Param("updatedBy") String updatedBy);

    // Mobile update
    @Modifying
    @Query("UPDATE UserEntity u SET u.mobile = :mobile, u.updatedAt = :now, u.updatedBy = :updatedBy WHERE u.id = :id")
    int updateMobile(@Param("id") String id,
                     @Param("mobile") String mobile,
                     @Param("now") Instant now,
                     @Param("updatedBy") String updatedBy);

    // NIC update
    @Modifying
    @Query("UPDATE UserEntity u SET u.nic = :nic, u.updatedAt = :now, u.updatedBy = :updatedBy WHERE u.id = :id")
    int updateNic(@Param("id") String id,
                  @Param("nic") String nic,
                  @Param("now") Instant now,
                  @Param("updatedBy") String updatedBy);

    // Activate user (NOT_VERIFIED -> ACTIVE) after OTP verification
    @Modifying
    @Query("UPDATE UserEntity u SET u.status = 'ACTIVE', u.updatedAt = :now WHERE u.id = :id")
    int activateUser(@Param("id") String id, @Param("now") Instant now);

    long countByUserType(UserType userType);
}
