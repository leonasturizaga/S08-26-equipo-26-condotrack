package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "access_logs", indexes = {@Index(name = "idx_access_logs_unit_id_occurred_at", columnList = "unit_id, occurred_at")})
@Getter @Setter @NoArgsConstructor
public class AccessLog extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "building_id", nullable = false) private Building building;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "unit_id", nullable = false) private Unit unit;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "visitor_id") private Visitor visitor;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "authorization_id") private VisitorAuthorization authorization;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "handled_by_staff_id") private Staff handledByStaff;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private Enums.AccessDirection direction;
    @Enumerated(EnumType.STRING) @Column(name = "access_method", nullable = false, length = 30) private Enums.AccessMethod accessMethod = Enums.AccessMethod.MANUAL;
    @Column(name = "occurred_at", nullable = false) private OffsetDateTime occurredAt;
    @Column(columnDefinition = "TEXT") private String notes;
}
