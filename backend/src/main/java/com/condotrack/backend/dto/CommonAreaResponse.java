//------------------ new PR32 M21 ------------------------
package com.condotrack.backend.dto;

import java.util.UUID;

public record CommonAreaResponse(
        UUID id,
        UUID buildingId,
        String buildingCode,
        String name,
        String areaType,
        Integer capacity,
        boolean bookingRequired,
        boolean active,
        Integer bookingDurationMinutes
) {
}