package com.condotrack.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record BuildingPageResponse(
        List<BuildingResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static BuildingPageResponse from(Page<BuildingResponse> page) {
        return new BuildingPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
