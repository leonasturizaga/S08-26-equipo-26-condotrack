package com.condotrack.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UnitUpdateRequest(
        @NotNull(message = "buildingId is required")
        UUID buildingId,

        @NotBlank(message = "unitNumber is required")
        @Size(max = 50, message = "unitNumber must not exceed 50 characters")
        String unitNumber,

        Integer floorNumber,

        @Size(max = 30, message = "unitType must not exceed 30 characters")
        String unitType,

        boolean active
) {
}
