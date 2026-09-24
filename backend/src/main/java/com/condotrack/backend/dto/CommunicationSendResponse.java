package com.condotrack.backend.dto;

import java.time.OffsetDateTime;

public record CommunicationSendResponse(
        String notificationType,
        String audienceType,
        String subject,
        int recipientCount,
        OffsetDateTime sentAt
) {
}
