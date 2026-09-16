package com.condotrack.backend.dto;

import com.condotrack.backend.model.Enums;
import jakarta.validation.constraints.NotNull;

public record DeliveryStatusUpdateRequest(
        @NotNull(message = "status is required")
        Enums.DeliveryStatus status
) {
}
