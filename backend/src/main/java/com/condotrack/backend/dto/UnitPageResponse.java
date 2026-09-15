package com.condotrack.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record UnitPageResponse(
        List<UnitResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static UnitPageResponse from(Page<UnitResponse> page) {
        return new UnitPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
