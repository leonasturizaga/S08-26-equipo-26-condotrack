package com.condotrack.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BuildingCreateRequest(
        @NotBlank(message = "name is required")
        @Size(max = 200, message = "name must not exceed 200 characters")
        String name,

        @NotBlank(message = "code is required")
        @Size(max = 50, message = "code must not exceed 50 characters")
        String code,

        @NotBlank(message = "addressLine1 is required")
        @Size(max = 255, message = "addressLine1 must not exceed 255 characters")
        String addressLine1,

        @Size(max = 255, message = "addressLine2 must not exceed 255 characters")
        String addressLine2,

        @NotBlank(message = "city is required")
        @Size(max = 120, message = "city must not exceed 120 characters")
        String city,

        @Size(max = 120, message = "stateProvince must not exceed 120 characters")
        String stateProvince,

        @Size(max = 30, message = "postalCode must not exceed 30 characters")
        String postalCode,

        @NotBlank(message = "country is required")
        @Size(max = 120, message = "country must not exceed 120 characters")
        String country,

        @Size(max = 80, message = "timezone must not exceed 80 characters")
        String timezone
) {
}
