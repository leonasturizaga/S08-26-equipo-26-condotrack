//------------------ original PR32 M21 no changes ------------------------
package com.condotrack.backend.dto;

import com.condotrack.backend.model.Enums;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookingStatusUpdateRequest(

        @NotNull(message = "status is required")
        Enums.BookingStatus status,

        @Size(max = 2000, message = "cancellationReason must not exceed 2000 characters")
        String cancellationReason
) {
}