package com.condotrack.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ReportsSummaryResponse(
        OffsetDateTime generatedAt,
        UUID buildingId,
        KpiSummary kpis,
        List<StatusMetric> statusMetrics,
        List<MonthlyTrendPoint> monthlyTrend,
        List<BuildingReportSummary> buildings
) {
    public record KpiSummary(
            long buildings,
            long activeUnits,
            long occupiedUnits,
            BigDecimal occupancyRate,
            long activeResidents,
            long accessEvents30d,
            long pendingDeliveries,
            long upcomingBookings30d,
            long openIncidents,
            long openMaintenance,
            long activeMoveRequests
    ) {}

    public record StatusMetric(String category, String status, long count) {}

    public record MonthlyTrendPoint(
            String month,
            long accessEvents,
            long deliveries,
            long bookings,
            long incidents,
            long maintenance,
            long moves
    ) {}

    public record BuildingReportSummary(
            UUID buildingId,
            String code,
            String name,
            long activeUnits,
            long occupiedUnits,
            BigDecimal occupancyRate,
            long activeResidents,
            long openIncidents,
            long openMaintenance,
            long pendingDeliveries
    ) {}
}
