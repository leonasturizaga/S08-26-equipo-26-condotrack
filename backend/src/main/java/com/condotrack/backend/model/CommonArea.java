package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "common_areas", uniqueConstraints = @UniqueConstraint(name = "uq_common_area_name", columnNames = {"building_id", "name"}))
@Getter @Setter @NoArgsConstructor
public class CommonArea extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "building_id", nullable = false) private Building building;
    @Column(nullable = false, length = 150) private String name;
    @Column(name = "area_type", nullable = false, length = 50) private String areaType;
    @Column private Integer capacity;
    @Column(name = "booking_required", nullable = false) private boolean bookingRequired = true;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "booking_duration_minutes") private Integer bookingDurationMinutes;
    @OneToMany(mappedBy = "commonArea", fetch = FetchType.LAZY) private List<Booking> bookings = new ArrayList<>();
}
