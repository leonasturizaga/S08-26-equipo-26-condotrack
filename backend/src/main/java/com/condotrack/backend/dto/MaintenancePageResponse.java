package com.condotrack.backend.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record MaintenancePageResponse(List<MaintenanceResponse> content, int page, int size, long totalElements, int totalPages) {
    public static MaintenancePageResponse from(Page<MaintenanceResponse> page) {
        return new MaintenancePageResponse(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
