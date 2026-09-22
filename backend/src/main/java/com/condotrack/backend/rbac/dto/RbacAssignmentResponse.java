package com.condotrack.backend.rbac.dto;

public record RbacAssignmentResponse(
        String roleCode,
        String permissionCode,
        boolean active,
        boolean overridden,
        String source
) {
}
