package com.condotrack.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MaintenanceResponse(
        UUID id, UUID buildingId, String buildingCode, UUID unitId, String unitNumber,
        UUID incidentId, UUID requestedByUserId, String requestedByName,
        UUID assignedToStaffId, String assignedToName, String assignedToStaffType, String assignedToEmployeeCode,
        String status, String priority, String category, String description,
        OffsetDateTime scheduledAt, OffsetDateTime completedAt, String resolution,
        OffsetDateTime createdAt, OffsetDateTime updatedAt
) {}
