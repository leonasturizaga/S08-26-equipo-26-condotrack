package com.condotrack.backend.dto;

import com.condotrack.backend.model.Enums;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record MoveStatusUpdateRequest(
        Enums.MoveRequestStatus status,
        OffsetDateTime scheduledStart,
        OffsetDateTime scheduledEnd,
        @Size(max = 10000, message = "notes must not exceed 10000 characters")
        String notes
) {}
