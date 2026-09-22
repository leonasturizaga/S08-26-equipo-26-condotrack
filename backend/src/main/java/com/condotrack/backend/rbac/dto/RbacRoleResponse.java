package com.condotrack.backend.rbac.dto;

import java.util.UUID;

public record RbacRoleResponse(UUID id, String code, String name) {
}
