package com.condotrack.backend.dto;

import com.condotrack.backend.model.CommonAreaBlockType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CommonAreaAvailabilityBlockResponse(
        UUID id,
        UUID commonAreaId,
        String commonAreaName,
        UUID buildingId,
        String buildingCode,
        CommonAreaBlockType blockType,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String notes,
        boolean active
) {
}