package com.condotrack.backend.dto;

import com.condotrack.backend.model.Enums;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DeliveryCreateRequest(
        @NotNull(message = "buildingId is required")
        UUID buildingId,

        @NotNull(message = "unitId is required")
        UUID unitId,

        @NotNull(message = "residentId is required")
        UUID residentId,

        @Size(max = 150, message = "carrierName must not exceed 150 characters")
        String carrierName,

        @Size(max = 150, message = "trackingNumber must not exceed 150 characters")
        String trackingNumber,

        @NotNull(message = "deliveryType is required")
        Enums.DeliveryType deliveryType,

        @Size(max = 2000, message = "notes must not exceed 2000 characters")
        String notes
) {
}
