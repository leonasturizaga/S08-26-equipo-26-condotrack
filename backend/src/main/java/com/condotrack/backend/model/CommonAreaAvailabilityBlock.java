package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "common_area_availability_blocks",
        indexes = {
                @Index(
                        name = "idx_ca_availability_blocks_area_start",
                        columnList = "common_area_id, start_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CommonAreaAvailabilityBlock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "common_area_id", nullable = false)
    private CommonArea commonArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", nullable = false, length = 30)
    private CommonAreaBlockType blockType;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private boolean active = true;
}