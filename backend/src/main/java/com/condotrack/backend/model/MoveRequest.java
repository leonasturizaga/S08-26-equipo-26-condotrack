package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "move_requests")
@Getter @Setter @NoArgsConstructor
public class MoveRequest extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "building_id", nullable = false) private Building building;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "unit_id", nullable = false) private Unit unit;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "resident_id") private Resident resident;
    @Enumerated(EnumType.STRING) @Column(name = "request_type", nullable = false, length = 20) private Enums.MoveRequestType requestType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Enums.MoveRequestStatus status = Enums.MoveRequestStatus.REQUESTED;
    @Column(name = "requested_at", nullable = false) private OffsetDateTime requestedAt;
    @Column(name = "scheduled_start") private OffsetDateTime scheduledStart;
    @Column(name = "scheduled_end") private OffsetDateTime scheduledEnd;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "approved_by_staff_id") private Staff approvedByStaff;
    @Column(name = "approved_at") private OffsetDateTime approvedAt;
    @Column(columnDefinition = "TEXT") private String notes;
}
