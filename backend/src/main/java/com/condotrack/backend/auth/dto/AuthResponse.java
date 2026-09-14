package com.condotrack.backend.auth.dto;

import java.util.List;
import java.util.UUID;

public record AuthResponse(
        String token,
        UserResponse user
) {
    public record UserResponse(
            UUID id,
            String email,
            String firstName,
            String lastName,
            String phone,
            List<String> roles
    ) {
    }
}
