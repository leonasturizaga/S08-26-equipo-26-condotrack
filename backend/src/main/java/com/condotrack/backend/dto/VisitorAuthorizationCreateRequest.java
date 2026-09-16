package com.condotrack.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VisitorAuthorizationCreateRequest(
        @NotNull(message = "unitId is required")
        UUID unitId,

        UUID residentId,

        @NotBlank(message = "visitor firstName is required")
        @Size(max = 100, message = "visitor firstName must not exceed 100 characters")
        String visitorFirstName,

        @NotBlank(message = "visitor lastName is required")
        @Size(max = 100, message = "visitor lastName must not exceed 100 characters")
        String visitorLastName,

        @Size(max = 40, message = "documentType must not exceed 40 characters")
        String documentType,

        @Size(max = 100, message = "documentNumber must not exceed 100 characters")
        String documentNumber,

        @Size(max = 50, message = "phone must not exceed 50 characters")
        String phone,

        @Size(max = 150, message = "companyName must not exceed 150 characters")
        String companyName,

        @Size(max = 255, message = "purpose must not exceed 255 characters")
        String purpose,

        @NotNull(message = "validFrom is required")
        OffsetDateTime validFrom,

        @NotNull(message = "validUntil is required")
        OffsetDateTime validUntil
) {
}
