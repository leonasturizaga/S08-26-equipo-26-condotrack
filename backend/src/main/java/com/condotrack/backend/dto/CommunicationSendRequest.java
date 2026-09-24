package com.condotrack.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CommunicationSendRequest(
        @NotBlank @Size(max = 255) String subject,
        @NotBlank @Size(max = 10000) String message,
        @NotBlank String audienceType,
        UUID buildingId,
        UUID unitId
) {
}
