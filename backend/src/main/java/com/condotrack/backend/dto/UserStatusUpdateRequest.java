package com.condotrack.backend.dto;

import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(
        @NotNull(message = "active is required")
        Boolean active
) {
}
