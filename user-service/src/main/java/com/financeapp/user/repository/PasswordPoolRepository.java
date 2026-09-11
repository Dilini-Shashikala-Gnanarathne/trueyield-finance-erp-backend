package com.financeapp.user.repository;

import com.financeapp.user.domain.entity.PasswordPoolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PasswordPoolRepository extends JpaRepository<PasswordPoolEntity, String> {

    /** Retrieves the N most recent password hashes for a user, ordered by recency. */
    @Query("""
        SELECT p FROM PasswordPoolEntity p
        WHERE p.user.id = :userId
        ORDER BY p.createdAt DESC
        LIMIT :limit
    """)
    List<PasswordPoolEntity> findRecentByUserId(@Param("userId") String userId, @Param("limit") int limit);

    /** Counts how many passwords are stored for this user. */
    long countByUserId(String userId);

    /** Deletes oldest entries beyond the retention limit. */
    @Modifying
    @Query(value = """
        DELETE FROM usr_password_pool
        WHERE user_id = :userId
        AND created_at < (
            SELECT created_at FROM (
                SELECT created_at FROM usr_password_pool
                WHERE user_id = :userId
                ORDER BY created_at DESC
                LIMIT 1 OFFSET :keepCount
            ) AS sub
        )
    """, nativeQuery = true)
    void trimPool(@Param("userId") String userId, @Param("keepCount") int keepCount);

    /** Convenience method to count by user id (field navigation). */
    @Query("SELECT COUNT(p) FROM PasswordPoolEntity p WHERE p.user.id = :userId")
    long countByUserEntityId(@Param("userId") String userId);
}
