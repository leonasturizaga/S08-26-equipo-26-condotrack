package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "staff")
@Getter @Setter @NoArgsConstructor
public class Staff extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "building_id", nullable = false) private Building building;
    @Enumerated(EnumType.STRING) @Column(name = "staff_type", nullable = false, length = 30) private Enums.StaffType staffType;
    @Column(name = "employee_code", length = 80) private String employeeCode;
    @Column(nullable = false) private boolean active = true;
}
