package com.condotrack.backend.dto;

import java.util.List;

public record ResidentPageResponse(
        List<ResidentResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
