package com.condotrack.backend.dto;

import com.condotrack.backend.model.Enums;

import java.util.UUID;

public record StaffAssignmentResponse(
        UUID id,
        UUID userId,
        UUID buildingId,
        String buildingCode,
        Enums.StaffType staffType,
        String employeeCode,
        boolean active
) {
}