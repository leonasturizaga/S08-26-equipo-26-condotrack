package com.condotrack.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record IncidentResponse(
        UUID id,
        UUID buildingId,
        String buildingCode,
        UUID unitId,
        String unitNumber,
        UUID reportedByUserId,
        String reportedByName,
        UUID assignedToStaffId,
        String assignedToName,
        String assignedToStaffType,
        String status,
        String severity,
        String title,
        String description,
        OffsetDateTime occurredAt,
        OffsetDateTime resolvedAt,
        String resolution,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
