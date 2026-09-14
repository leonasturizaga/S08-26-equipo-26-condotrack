package com.condotrack.backend.auth.dto;

public record RegistrationResponse(
        String message,
        AuthResponse.UserResponse user
) {
}
