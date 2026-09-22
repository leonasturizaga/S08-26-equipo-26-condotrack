package com.condotrack.backend.rbac.dto;

import java.util.List;

public record RbacAuditPageResponse(
        List<RbacAuditResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
