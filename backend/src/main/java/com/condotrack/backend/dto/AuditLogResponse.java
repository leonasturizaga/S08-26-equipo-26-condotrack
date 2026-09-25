package com.condotrack.backend.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        OffsetDateTime occurredAt,
        UUID buildingId,
        String buildingCode,
        String entityType,
        UUID entityId,
        String action,
        String actorUserId,
        String actorEmail,
        String previousStatus,
        String newStatus,
        String resolution,
        Map<String, Object> details
) {}
