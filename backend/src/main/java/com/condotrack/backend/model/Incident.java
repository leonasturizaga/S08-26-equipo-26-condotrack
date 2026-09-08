package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "incidents")
@Getter @Setter @NoArgsConstructor
public class Incident extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "building_id", nullable = false) private Building building;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "unit_id", nullable = false) private Unit unit;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "reported_by_user_id", nullable = false) private User reportedByUser;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to_staff_id") private Staff assignedToStaff;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Enums.IncidentStatus status = Enums.IncidentStatus.CREATED;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Enums.Severity severity = Enums.Severity.MEDIUM;
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String description;
    @Column(name = "occurred_at") private OffsetDateTime occurredAt;
    @Column(name = "resolved_at") private OffsetDateTime resolvedAt;
    @Column(columnDefinition = "TEXT") private String resolution;
}
