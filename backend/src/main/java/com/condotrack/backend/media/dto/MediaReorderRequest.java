package com.condotrack.backend.media.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record MediaReorderRequest(
        @NotEmpty(message = "mediaIds must contain at least one media ID")
        List<UUID> mediaIds
) {
}
