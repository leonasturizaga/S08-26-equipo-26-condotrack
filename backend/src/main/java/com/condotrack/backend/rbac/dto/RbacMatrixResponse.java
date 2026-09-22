package com.condotrack.backend.rbac.dto;

import java.util.List;
import java.util.UUID;

public record RbacMatrixResponse(
        UUID buildingId,
        String scope,
        List<RbacRoleResponse> roles,
        List<RbacPermissionResponse> permissions,
        List<RbacAssignmentResponse> assignments
) {
}
