//------------------ original PR32 M21 ------------------------
package com.condotrack.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID buildingId,
        String buildingCode,
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
        OffsetDateTime approvedAt,
        UUID approvedByStaffId,
        String cancellationReason
) {
}