package com.condotrack.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VisitorAuthorizationResponse(
        UUID id,
        UUID buildingId,
        UUID unitId,
        String unitNumber,
        UUID residentId,
        UUID visitorId,
        String visitorFirstName,
        String visitorLastName,
        String status,
        String qrToken,
        OffsetDateTime validFrom,
        OffsetDateTime validUntil,
        String purpose
) {
}
