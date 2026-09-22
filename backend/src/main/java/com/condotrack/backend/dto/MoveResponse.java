package com.condotrack.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MoveResponse(
        UUID id,
        UUID buildingId,
        String buildingCode,
        UUID unitId,
        String unitNumber,
        UUID residentId,
        String residentName,
        String residentType,
        String requestType,
        String status,
        OffsetDateTime requestedAt,
        OffsetDateTime scheduledStart,
        OffsetDateTime scheduledEnd,
        UUID approvedByStaffId,
        String approvedByStaffName,
        OffsetDateTime approvedAt,
        boolean ownerAuthorized,
        UUID ownerAuthorizedByUserId,
        String ownerAuthorizedByUserName,
        OffsetDateTime ownerAuthorizedAt,
        String ownerAuthorizationNotes,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
