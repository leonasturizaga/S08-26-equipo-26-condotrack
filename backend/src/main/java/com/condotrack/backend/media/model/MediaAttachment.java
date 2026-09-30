package com.condotrack.backend.media.model;

import com.condotrack.backend.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "media_attachments", indexes = {
        @Index(name = "idx_media_attachments_entity", columnList = "entity_type, entity_id, active"),
        @Index(name = "idx_media_attachments_media", columnList = "media_id")
})
@Getter
@Setter
@NoArgsConstructor
public class MediaAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    private MediaEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MediaPurpose purpose;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(nullable = false)
    private boolean active = true;
}
