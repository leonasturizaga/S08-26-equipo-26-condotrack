package com.condotrack.backend.dto;

import java.util.UUID;

public record BuildingResponse(
        UUID id,
        String name,
        String code,
        String addressLine1,
        String addressLine2,
        String city,
        String stateProvince,
        String postalCode,
        String country,
        String timezone,
        boolean active
) {
}
