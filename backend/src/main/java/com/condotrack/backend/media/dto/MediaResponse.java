package com.condotrack.backend.media.dto;

import com.condotrack.backend.media.model.MediaEntityType;
import com.condotrack.backend.media.model.MediaPurpose;
import com.condotrack.backend.media.model.MediaType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MediaResponse(
        UUID id,
        UUID buildingId,
        MediaType mediaType,
        MediaEntityType entityType,
        UUID entityId,
        MediaPurpose purpose,
        int sortOrder,
        String originalFilename,
        String contentType,
        String fileFormat,
        long fileSize,
        Integer width,
        Integer height,
        Double durationSeconds,
        String secureUrl,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
