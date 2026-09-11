package com.financeapp.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usr_city")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CityEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "zipcode", length = 20)
    private String zipcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private DistrictEntity district;
}
