package com.condotrack.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record QrAccessRequest(
        @NotBlank(message = "qrToken is required")
        String qrToken
) {
}
