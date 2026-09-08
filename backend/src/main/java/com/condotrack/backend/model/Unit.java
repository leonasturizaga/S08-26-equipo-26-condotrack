package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "units", uniqueConstraints = @UniqueConstraint(name = "uq_units_building_number", columnNames = {"building_id", "unit_number"}))
@Getter @Setter @NoArgsConstructor
public class Unit extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "unit_number", nullable = false, length = 50) private String unitNumber;
    @Column(name = "floor_number") private Integer floorNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, length = 30)
    private Enums.UnitType unitType = Enums.UnitType.RESIDENTIAL;

    @Column(nullable = false) private boolean active = true;

    @OneToMany(mappedBy = "unit", fetch = FetchType.LAZY) private List<Resident> residents = new ArrayList<>();
    @OneToMany(mappedBy = "unit", fetch = FetchType.LAZY) private List<Delivery> deliveries = new ArrayList<>();
    @OneToMany(mappedBy = "unit", fetch = FetchType.LAZY) private List<Booking> bookings = new ArrayList<>();
    @OneToMany(mappedBy = "unit", fetch = FetchType.LAZY) private List<Incident> incidents = new ArrayList<>();
    @OneToMany(mappedBy = "unit", fetch = FetchType.LAZY) private List<MaintenanceRequest> maintenanceRequests = new ArrayList<>();
}
