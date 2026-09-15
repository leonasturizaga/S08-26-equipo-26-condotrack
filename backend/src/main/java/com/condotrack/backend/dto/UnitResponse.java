package com.condotrack.backend.dto;

import java.util.UUID;

public record UnitResponse(
        UUID id,
        UUID buildingId,
        String unitNumber,
        Integer floorNumber,
        String unitType,
        boolean active
) {
}
