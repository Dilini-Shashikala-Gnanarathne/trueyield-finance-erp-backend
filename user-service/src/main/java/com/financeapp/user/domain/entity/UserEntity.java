package com.financeapp.user.domain.entity;

import com.financeapp.user.domain.enums.UserStatus;
import com.financeapp.user.domain.enums.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

/**
 * Core user entity.
 *
 * Soft-delete pattern:
 *   - @SQLDelete intercepts DELETE statements and sets deleted_at + status='DELETED'
 *   - @SQLRestriction ensures all SELECT queries automatically filter deleted records
 *
 * Audit trail:
 *   - registered_at / registered_by captured at creation
 *   - updated_at / updated_by maintained by AuditableEntity base class
 */
@Entity
@Table(name = "usr_user")
@SQLDelete(sql = "UPDATE usr_user SET deleted_at = CURRENT_TIMESTAMP, status = 'DELETED' WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "mobile", nullable = false, length = 15)
    private String mobile;

    @Column(name = "nic", length = 20)
    private String nic;

    @Column(name = "email", length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.NOT_VERIFIED;

    @Column(name = "force_password_reset", nullable = false)
    private boolean forcePasswordReset = false;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    @Column(name = "registered_by", updatable = false, length = 36)
    private String registeredBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Column(name = "last_password_reset_at")
    private Instant lastPasswordResetAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", length = 36)
    private String deletedBy;

    // ----- Relationships -----

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private StudentEntity student;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private AdminEntity admin;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private ProfileEntity profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private AddressEntity address;

    @PrePersist
    public void prePersist() {
        if (registeredAt == null) {
            registeredAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
