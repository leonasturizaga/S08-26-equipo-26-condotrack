package com.condotrack.backend.dto;

import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phone,
        boolean active,
        List<String> roles
) {
}
