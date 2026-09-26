//------------------ M23 ------------------------
package com.condotrack.backend.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record UnitDashboardResponse(
        UnitSummary unit,
        List<ResidentSummary> residents,
        List<AccessLogSummary> accessHistory,
        List<DeliverySummary> pendingDeliveries,
        List<BookingSummary> bookings,
        List<MoveRequestSummary> activeMoveRequests,
        List<IncidentSummary> incidents,
        List<MaintenanceSummary> maintenanceRequests
) {
    public record UnitSummary(UUID id, UUID buildingId, String buildingName, String unitNumber, Integer floorNumber, String unitType) {}
    public record ResidentSummary(UUID id, UUID userId, String firstName, String lastName, String residentType, boolean primaryContact) {}
    public record AccessLogSummary(UUID id, String direction, String accessMethod, OffsetDateTime occurredAt, UUID visitorId, UUID authorizationId) {}
    public record DeliverySummary(UUID id, String deliveryType, String status, String carrierName, String trackingNumber, OffsetDateTime receivedAt, OffsetDateTime notifiedAt) {}
    public record BookingSummary(UUID id, UUID commonAreaId, String commonAreaName, String status, OffsetDateTime startAt, OffsetDateTime endAt, String purpose) {}
    public record MoveRequestSummary(UUID id, String requestType, String status, OffsetDateTime requestedAt, OffsetDateTime scheduledStart, OffsetDateTime scheduledEnd) {}
    public record IncidentSummary(UUID id, String status, String severity, String title, OffsetDateTime occurredAt, OffsetDateTime resolvedAt, String resolution) {}
    public record MaintenanceSummary(UUID id, UUID incidentId, String status, String priority, String category, OffsetDateTime scheduledAt, OffsetDateTime completedAt, String description, String resolution) {}
}
