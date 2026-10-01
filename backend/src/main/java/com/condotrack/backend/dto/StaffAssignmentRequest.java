package com.condotrack.backend.dto;

import com.condotrack.backend.model.Enums;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StaffAssignmentRequest(
        @NotNull UUID buildingId,
        @NotNull Enums.StaffType staffType,
        String employeeCode,
        Boolean active
) {
}