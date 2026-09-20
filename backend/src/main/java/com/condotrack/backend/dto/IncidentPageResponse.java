package com.condotrack.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record IncidentPageResponse(
        List<IncidentResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static IncidentPageResponse from(Page<IncidentResponse> page) {
        return new IncidentPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
