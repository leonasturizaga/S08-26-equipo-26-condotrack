package com.condotrack.backend.rbac.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record RbacPermissionUpdateRequest(
        UUID buildingId,
        @NotBlank String roleCode,
        @NotBlank String permissionCode,
        boolean active
) {
}
