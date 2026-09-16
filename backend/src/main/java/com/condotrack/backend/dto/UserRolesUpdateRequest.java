package com.condotrack.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record UserRolesUpdateRequest(
        @NotEmpty(message = "at least one role is required")
        Set<@NotBlank(message = "role code must not be blank") String> roles
) {
}
