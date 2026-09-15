package com.condotrack.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResidentUnitAssignmentUpdateRequest(
        @NotNull(message = "unitId is required")
        UUID unitId
) {
}
