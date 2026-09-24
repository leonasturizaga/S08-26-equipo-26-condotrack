package com.condotrack.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CommunicationResponse(
        UUID id,
        String subject,
        String message,
        String audienceType,
        UUID buildingId,
        String buildingCode,
        UUID unitId,
        String unitNumber,
        UUID sentByUserId,
        String sentByName,
        OffsetDateTime sentAt,
        long recipientCount,
        long readCount,
        long unreadCount
) {
}
