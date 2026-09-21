package com.condotrack.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID buildingId,
        UUID commonAreaId,
        String commonAreaName,
        UUID unitId,
        String unitNumber,
        UUID residentId,
        String residentName,
        String status,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String purpose,
        UUID approvedByStaffId,
        OffsetDateTime approvedAt,
        String cancellationReason
) {
}