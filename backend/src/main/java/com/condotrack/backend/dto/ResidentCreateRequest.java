package com.condotrack.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ResidentCreateRequest(
        @NotNull(message = "userId is required")
        UUID userId,

        @NotNull(message = "unitId is required")
        UUID unitId,

        @NotNull(message = "residentType is required")
        String residentType,

        LocalDate moveInDate,
        LocalDate moveOutDate,
        boolean primaryContact
) {
}
