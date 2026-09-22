package com.condotrack.backend.rbac.dto;

import java.util.UUID;

public record RbacPermissionResponse(
        UUID id,
        String code,
        String name,
        String description,
        String module,
        String action,
        String scope
) {
}
