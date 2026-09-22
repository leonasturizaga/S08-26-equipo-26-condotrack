package com.condotrack.backend.rbac.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RbacAuditResponse(
        UUID id,
        OffsetDateTime occurredAt,
        UUID buildingId,
        String buildingCode,
        String action,
        String roleCode,
        String permissionCode,
        Boolean previousActive,
        Boolean newActive,
        String scope,
        String actorEmail
) {
}
