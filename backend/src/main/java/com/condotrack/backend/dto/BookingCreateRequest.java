//------------------ original PR32 M21 no changes ------------------------
package com.condotrack.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingCreateRequest(

        @NotNull(message = "buildingId is required")
        UUID buildingId,

        @NotNull(message = "commonAreaId is required")
        UUID commonAreaId,

        @NotNull(message = "unitId is required")
        UUID unitId,

        @NotNull(message = "residentId is required")
        UUID residentId,

        @NotNull(message = "startAt is required")
        OffsetDateTime startAt,

        @NotNull(message = "endAt is required")
        OffsetDateTime endAt,

        @Size(max = 255, message = "purpose must not exceed 255 characters")
        String purpose
) {
}