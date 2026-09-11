package com.accmaster.usersvc.domain.entity;

import com.accmaster.usersvc.domain.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Student-specific profile details. One-to-one with UserEntity. */
@Entity
@Table(name = "usr_student")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentEntity {

    @Id
    @Column(name = "user_id", length = 36)
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "academic_id", nullable = false, unique = true, length = 50)
    private String academicId;

    @Column(name = "fname", nullable = false, length = 20)
    private String fname;

    @Column(name = "lname", nullable = false, length = 20)
    private String lname;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender = Gender.NOT_SET;

    @Column(name = "whatsapp_number", length = 15)
    private String whatsappNumber;

    @Column(name = "school", length = 150)
    private String school;

    @Column(name = "guardian_name", length = 100)
    private String guardianName;

    @Column(name = "guardian_mobile", length = 15)
    private String guardianMobile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usr_batch_id")
    private BatchEntity batch;

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
