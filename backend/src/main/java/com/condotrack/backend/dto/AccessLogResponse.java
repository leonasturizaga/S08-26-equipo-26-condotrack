package com.condotrack.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AccessLogResponse(
        UUID id,
        UUID buildingId,
        UUID unitId,
        String unitNumber,
        UUID visitorId,
        UUID authorizationId,
        String visitorFirstName,
        String visitorLastName,
        String direction,
        String accessMethod,
        OffsetDateTime occurredAt
) {
}
