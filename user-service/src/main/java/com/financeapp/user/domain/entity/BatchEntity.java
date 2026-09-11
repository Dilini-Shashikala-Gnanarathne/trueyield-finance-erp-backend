package com.financeapp.user.domain.entity;

import com.financeapp.user.domain.enums.BatchStatus;
import jakarta.persistence.*;
import lombok.*;

/** Academic batch/cohort that students are assigned to. */
@Entity
@Table(name = "usr_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BatchStatus status = BatchStatus.ACTIVE;
}
