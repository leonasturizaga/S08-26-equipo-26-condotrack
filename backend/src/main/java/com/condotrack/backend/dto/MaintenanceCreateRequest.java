package com.condotrack.backend.dto;

import com.condotrack.backend.model.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MaintenanceCreateRequest(
        @NotNull(message = "unitId is required") UUID unitId,
        UUID incidentId,
        @NotNull(message = "priority is required") Enums.Priority priority,
        @NotBlank(message = "category is required") @Size(max = 60, message = "category must not exceed 60 characters") String category,
        @NotBlank(message = "description is required") @Size(max = 10000, message = "description must not exceed 10000 characters") String description,
        OffsetDateTime scheduledAt
) {}
