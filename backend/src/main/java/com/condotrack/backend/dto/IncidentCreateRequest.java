package com.condotrack.backend.dto;

import com.condotrack.backend.model.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record IncidentCreateRequest(
        @NotNull(message = "unitId is required")
        UUID unitId,

        @NotBlank(message = "title is required")
        @Size(max = 200, message = "title must not exceed 200 characters")
        String title,

        @NotBlank(message = "description is required")
        @Size(max = 10000, message = "description must not exceed 10000 characters")
        String description,

        @NotNull(message = "severity is required")
        Enums.Severity severity,

        OffsetDateTime occurredAt
) {
}
