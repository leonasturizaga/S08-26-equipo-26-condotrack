package com.condotrack.backend.dto;

import java.util.UUID;

public record UnitLookupResponse(
        UUID id,
        UUID buildingId,
        String buildingName,
        String buildingCode,
        String unitNumber,
        Integer floorNumber,
        String unitType
) {}
