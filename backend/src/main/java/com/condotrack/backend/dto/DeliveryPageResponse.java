package com.condotrack.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record DeliveryPageResponse(
        List<DeliveryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static DeliveryPageResponse from(Page<DeliveryResponse> page) {
        return new DeliveryPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
