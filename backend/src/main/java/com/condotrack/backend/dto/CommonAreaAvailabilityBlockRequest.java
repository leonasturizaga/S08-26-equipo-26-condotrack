package com.condotrack.backend.dto;

import com.condotrack.backend.model.CommonAreaBlockType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CommonAreaAvailabilityBlockRequest(

        @NotNull(message = "blockType is required")
        CommonAreaBlockType blockType,

        @NotNull(message = "startAt is required")
        OffsetDateTime startAt,

        @NotNull(message = "endAt is required")
        OffsetDateTime endAt,

        @Size(max = 2000, message = "notes must not exceed 2000 characters")
        String notes
) {
}