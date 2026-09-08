package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "residents")
@Getter @Setter @NoArgsConstructor
public class Resident extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "unit_id", nullable = false) private Unit unit;

    @Enumerated(EnumType.STRING) @Column(name = "resident_type", nullable = false, length = 30) private Enums.ResidentType residentType;
    @Column(name = "move_in_date") private LocalDate moveInDate;
    @Column(name = "move_out_date") private LocalDate moveOutDate;
    @Column(name = "primary_contact", nullable = false) private boolean primaryContact = false;
    @Column(nullable = false) private boolean active = true;
}
