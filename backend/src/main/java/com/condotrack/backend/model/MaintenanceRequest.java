package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "maintenance_requests")
@Getter @Setter @NoArgsConstructor
public class MaintenanceRequest extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "building_id", nullable = false) private Building building;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "unit_id", nullable = false) private Unit unit;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "incident_id") private Incident incident;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "requested_by_user_id", nullable = false) private User requestedByUser;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to_staff_id") private Staff assignedToStaff;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Enums.MaintenanceStatus status = Enums.MaintenanceStatus.CREATED;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Enums.Priority priority = Enums.Priority.MEDIUM;
    @Column(nullable = false, length = 60) private String category;
    @Column(nullable = false, columnDefinition = "TEXT") private String description;
    @Column(name = "scheduled_at") private OffsetDateTime scheduledAt;
    @Column(name = "completed_at") private OffsetDateTime completedAt;
    @Column(columnDefinition = "TEXT") private String resolution;
}
