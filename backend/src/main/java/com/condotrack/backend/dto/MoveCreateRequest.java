package com.condotrack.backend.dto;

import com.condotrack.backend.model.Enums;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MoveCreateRequest(
        @NotNull(message = "unitId is required")
        UUID unitId,

        UUID residentId,

        @NotNull(message = "requestType is required")
        Enums.MoveRequestType requestType,

        OffsetDateTime scheduledStart,
        OffsetDateTime scheduledEnd,

        @Size(max = 10000, message = "notes must not exceed 10000 characters")
        String notes
) {}
