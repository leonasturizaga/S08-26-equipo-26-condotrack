package com.condotrack.backend.dto;

import java.util.List;

public record MovePageResponse(
        List<MoveResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
