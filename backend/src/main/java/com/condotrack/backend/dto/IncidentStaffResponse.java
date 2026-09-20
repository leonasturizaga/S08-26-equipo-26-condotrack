package com.condotrack.backend.dto;

import java.util.UUID;

public record IncidentStaffResponse(
        UUID staffId,
        UUID userId,
        UUID buildingId,
        String firstName,
        String lastName,
        String staffType,
        String employeeCode
) {
}
