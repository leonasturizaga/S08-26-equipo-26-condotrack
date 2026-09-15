package com.condotrack.backend.dto;

import java.util.UUID;

public record ResidentResponse(
        UUID id,
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String phone,
        UUID unitId,
        UUID buildingId,
        String unitNumber,
        String residentType,
        String moveInDate,
        String moveOutDate,
        boolean primaryContact,
        boolean active
) {
}
