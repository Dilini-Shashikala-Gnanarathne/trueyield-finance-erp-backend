package com.accmaster.usersvc.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usr_district")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DistrictEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;
}
