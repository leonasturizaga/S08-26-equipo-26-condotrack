package com.condotrack.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 255, message = "email must not exceed 255 characters")
        String email,

        @NotBlank(message = "firstName is required")
        @Size(max = 100, message = "firstName must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "lastName is required")
        @Size(max = 100, message = "lastName must not exceed 100 characters")
        String lastName,

        @Size(max = 50, message = "phone must not exceed 50 characters")
        String phone
) {
}
