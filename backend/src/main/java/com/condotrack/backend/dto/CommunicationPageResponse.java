package com.condotrack.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record CommunicationPageResponse(
        List<CommunicationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static CommunicationPageResponse from(Page<CommunicationResponse> page) {
        return new CommunicationPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
