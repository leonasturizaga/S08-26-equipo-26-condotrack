package com.condotrack.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID buildingId,
        String buildingCode,
        String notificationType,
        String status,
        String channel,
        String subject,
        String message,
        String relatedEntityType,
        UUID relatedEntityId,
        OffsetDateTime sentAt,
        OffsetDateTime readAt,
        OffsetDateTime createdAt
) {
}
