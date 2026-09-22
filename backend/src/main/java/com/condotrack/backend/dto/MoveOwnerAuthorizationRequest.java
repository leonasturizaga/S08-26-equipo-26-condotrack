package com.condotrack.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MoveOwnerAuthorizationRequest(
        @NotNull(message = "authorized is required")
        Boolean authorized,

        @Size(max = 5000, message = "notes must not exceed 5000 characters")
        String notes
) {}
