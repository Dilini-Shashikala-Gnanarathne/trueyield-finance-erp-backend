package com.financeapp.user.domain.entity;

import com.financeapp.user.domain.enums.AdminRole;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/** Admin-specific profile details. One-to-one with UserEntity. */
@Entity
@Table(name = "usr_admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminEntity {

    @Id
    @Column(name = "user_id", length = 36)
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "fname", nullable = false, length = 20)
    private String fname;

    @Column(name = "lname", nullable = false, length = 20)
    private String lname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private AdminRole role;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @PrePersist
    @PreUpdate
    public void touch() {
        updatedAt = Instant.now();
    }
}
