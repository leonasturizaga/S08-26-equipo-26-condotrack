package com.condotrack.backend.dto;

import java.util.UUID;

public record AmenityResponse(
        UUID id,
        String code,
        String name
) {
}