package com.condotrack.backend.dto;

import jakarta.validation.constraints.Size;

public record ResidentUpdateRequest(
        @Size(max = 100, message = "firstName must not exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "lastName must not exceed 100 characters")
        String lastName,

        @Size(max = 50, message = "phone must not exceed 50 characters")
        String phone
) {
}
