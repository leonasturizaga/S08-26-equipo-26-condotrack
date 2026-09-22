package com.condotrack.backend.dto;

import java.util.UUID;

public record MoveUnitOptionResponse(
        UUID id,
        String unitNumber,
        UUID buildingId,
        String buildingCode
) {}
