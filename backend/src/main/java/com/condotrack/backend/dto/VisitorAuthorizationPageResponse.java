package com.condotrack.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record VisitorAuthorizationPageResponse(
        List<VisitorAuthorizationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static VisitorAuthorizationPageResponse from(Page<VisitorAuthorizationResponse> page) {
        return new VisitorAuthorizationPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
