package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "buildings")
@Getter @Setter @NoArgsConstructor
public class Building extends BaseEntity {
    @Column(nullable = false, length = 200) private String name;
    @Column(nullable = false, unique = true, length = 50) private String code;
    @Column(name = "address_line_1", nullable = false) private String addressLine1;
    @Column(name = "address_line_2") private String addressLine2;
    @Column(nullable = false, length = 120) private String city;
    @Column(name = "state_province", length = 120) private String stateProvince;
    @Column(name = "postal_code", length = 30) private String postalCode;
    @Column(nullable = false, length = 120) private String country;
    @Column(nullable = false, length = 80) private String timezone = "UTC";
    @Column(nullable = false) private boolean active = true;

    @OneToMany(mappedBy = "building", fetch = FetchType.LAZY)
    private List<Unit> units = new ArrayList<>();
}
