package com.condotrack.backend.dto;

import java.util.UUID;

public record IncidentUnitOptionResponse(
        UUID unitId,
        String unitNumber,
        UUID buildingId,
        String buildingCode
) {
}
