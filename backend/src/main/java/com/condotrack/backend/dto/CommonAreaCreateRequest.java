package com.condotrack.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CommonAreaCreateRequest(

        @NotNull(message = "buildingId is required")
        UUID buildingId,

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must not exceed 150 characters")
        String name,

        @NotBlank(message = "areaType is required")
        @Size(max = 50, message = "areaType must not exceed 50 characters")
        String areaType,

        @Min(value = 1, message = "capacity must be at least 1")
        Integer capacity,

        boolean bookingRequired,

        @Min(value = 1, message = "bookingDurationMinutes must be at least 1")
        Integer bookingDurationMinutes
) {
}