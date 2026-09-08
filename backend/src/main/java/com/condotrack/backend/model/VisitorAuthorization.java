package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "visitor_authorizations")
@Getter @Setter @NoArgsConstructor
public class VisitorAuthorization extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "building_id", nullable = false) private Building building;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "unit_id", nullable = false) private Unit unit;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "resident_id") private Resident resident;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "visitor_id", nullable = false) private Visitor visitor;
    @Column(name = "qr_token", unique = true, length = 255) private String qrToken;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Enums.VisitorAuthorizationStatus status = Enums.VisitorAuthorizationStatus.PENDING;
    @Column(name = "valid_from", nullable = false) private OffsetDateTime validFrom;
    @Column(name = "valid_until", nullable = false) private OffsetDateTime validUntil;
    @Column(length = 255) private String purpose;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "approved_by") private User approvedBy;
    @Column(name = "approved_at") private OffsetDateTime approvedAt;
}
