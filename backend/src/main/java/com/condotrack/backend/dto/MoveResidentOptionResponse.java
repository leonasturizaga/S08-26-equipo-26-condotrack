package com.condotrack.backend.dto;

import java.util.UUID;

public record MoveResidentOptionResponse(
        UUID id,
        UUID unitId,
        String unitNumber,
        String firstName,
        String lastName,
        String residentType
) {}
