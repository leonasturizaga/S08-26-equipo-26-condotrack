package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "bookings", indexes = {@Index(name = "idx_bookings_unit_id_start_at", columnList = "unit_id, start_at")})
@Getter @Setter @NoArgsConstructor
public class Booking extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "building_id", nullable = false) private Building building;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "common_area_id", nullable = false) private CommonArea commonArea;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "unit_id", nullable = false) private Unit unit;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "resident_id") private Resident resident;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Enums.BookingStatus status = Enums.BookingStatus.PENDING;
    @Column(name = "start_at", nullable = false) private OffsetDateTime startAt;
    @Column(name = "end_at", nullable = false) private OffsetDateTime endAt;
    @Column(length = 255) private String purpose;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "approved_by_staff_id") private Staff approvedByStaff;
    @Column(name = "approved_at") private OffsetDateTime approvedAt;
    @Column(name = "cancellation_reason", columnDefinition = "TEXT") private String cancellationReason;
}
