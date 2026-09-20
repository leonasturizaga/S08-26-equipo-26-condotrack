package com.condotrack.backend.dto;

import com.condotrack.backend.model.Enums;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidentStatusUpdateRequest(
        @NotNull(message = "status is required")
        Enums.IncidentStatus status,

        @Size(max = 10000, message = "resolution must not exceed 10000 characters")
        String resolution
) {
}
