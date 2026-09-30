package com.condotrack.backend.media.model;

import com.condotrack.backend.model.BaseEntity;
import com.condotrack.backend.model.Building;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "media", indexes = {
        @Index(name = "idx_media_building_id", columnList = "building_id"),
        @Index(name = "idx_media_cloudinary_public_id", columnList = "cloudinary_public_id"),
        @Index(name = "idx_media_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
public class Media extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private Building building;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", nullable = false, length = 30)
    private StorageProvider storageProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    @Column(name = "cloudinary_asset_id", nullable = false, unique = true, length = 100)
    private String cloudinaryAssetId;

    @Column(name = "cloudinary_public_id", nullable = false, unique = true, length = 500)
    private String cloudinaryPublicId;

    @Column(name = "cloudinary_resource_type", nullable = false, length = 20)
    private String cloudinaryResourceType;

    @Column(name = "cloudinary_delivery_type", nullable = false, length = 20)
    private String cloudinaryDeliveryType;

    @Column(name = "secure_url", nullable = false, columnDefinition = "TEXT")
    private String secureUrl;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "file_format", length = 30)
    private String fileFormat;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "duration_seconds")
    private Double durationSeconds;

    @Column(nullable = false)
    private boolean active = true;
}
