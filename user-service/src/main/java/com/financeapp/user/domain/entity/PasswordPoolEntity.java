package com.financeapp.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Password history pool — retains last N hashed passwords to prevent reuse.
 * PASSWORD_HISTORY_LIMIT = 3 (matches PHP PasswordPoolRepository::PASSWORD_HISTORY_LIMIT).
 */
@Entity
@Table(name = "usr_password_pool")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordPoolEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }
}
