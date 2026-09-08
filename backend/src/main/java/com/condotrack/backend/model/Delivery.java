package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "deliveries")
@Getter @Setter @NoArgsConstructor
public class Delivery extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "building_id", nullable = false) private Building building;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "unit_id", nullable = false) private Unit unit;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "resident_id") private Resident resident;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "received_by_staff_id") private Staff receivedByStaff;
    @Column(name = "carrier_name", length = 150) private String carrierName;
    @Column(name = "tracking_number", length = 150) private String trackingNumber;
    @Enumerated(EnumType.STRING) @Column(name = "delivery_type", nullable = false, length = 30) private Enums.DeliveryType deliveryType = Enums.DeliveryType.PACKAGE;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Enums.DeliveryStatus status = Enums.DeliveryStatus.RECEIVED;
    @Column(name = "received_at", nullable = false) private OffsetDateTime receivedAt;
    @Column(name = "notified_at") private OffsetDateTime notifiedAt;
    @Column(name = "collected_at") private OffsetDateTime collectedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "collected_by_user_id") private User collectedByUser;
    @Column(columnDefinition = "TEXT") private String notes;
}
