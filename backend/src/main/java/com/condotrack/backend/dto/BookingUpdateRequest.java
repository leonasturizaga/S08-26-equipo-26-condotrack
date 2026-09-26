package com.condotrack.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record BookingUpdateRequest(
        @NotNull(message = "startAt is required")
        OffsetDateTime startAt,

        @NotNull(message = "endAt is required")
        OffsetDateTime endAt,

        @Size(max = 255, message = "purpose must not exceed 255 characters")
        String purpose
) {
}
