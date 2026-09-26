package com.condotrack.backend.repository;

import com.condotrack.backend.dto.ReportsSummaryResponse.BuildingReportSummary;
import com.condotrack.backend.dto.ReportsSummaryResponse.KpiSummary;
import com.condotrack.backend.dto.ReportsSummaryResponse.MonthlyTrendPoint;
import com.condotrack.backend.dto.ReportsSummaryResponse.StatusMetric;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Repository
public class ReportsRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public ReportsRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public KpiSummary loadKpis(UUID buildingId) {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM buildings b WHERE b.active = true AND (:building_id IS NULL OR b.id = :building_id)) AS buildings,
                    (SELECT COUNT(*) FROM units u JOIN buildings b ON b.id = u.building_id WHERE u.active = true AND b.active = true AND (:building_id IS NULL OR u.building_id = :building_id)) AS active_units,
                    (SELECT COUNT(DISTINCT r.unit_id) FROM residents r JOIN units u ON u.id = r.unit_id JOIN buildings b ON b.id = u.building_id WHERE r.active = true AND u.active = true AND b.active = true AND (:building_id IS NULL OR u.building_id = :building_id)) AS occupied_units,
                    (SELECT COUNT(*) FROM residents r JOIN units u ON u.id = r.unit_id JOIN buildings b ON b.id = u.building_id WHERE r.active = true AND u.active = true AND b.active = true AND (:building_id IS NULL OR u.building_id = :building_id)) AS active_residents,
                    (SELECT COUNT(*) FROM access_logs a WHERE a.occurred_at >= CURRENT_TIMESTAMP - INTERVAL '30 days' AND (:building_id IS NULL OR a.building_id = :building_id)) AS access_events_30d,
                    (SELECT COUNT(*) FROM deliveries d WHERE d.status IN ('RECEIVED', 'NOTIFIED') AND (:building_id IS NULL OR d.building_id = :building_id)) AS pending_deliveries,
                    (SELECT COUNT(*) FROM bookings b WHERE b.status IN ('PENDING', 'APPROVED') AND b.start_at >= CURRENT_TIMESTAMP AND b.start_at < CURRENT_TIMESTAMP + INTERVAL '30 days' AND (:building_id IS NULL OR b.building_id = :building_id)) AS upcoming_bookings_30d,
                    (SELECT COUNT(*) FROM incidents i WHERE i.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED') AND (:building_id IS NULL OR i.building_id = :building_id)) AS open_incidents,
                    (SELECT COUNT(*) FROM maintenance_requests m WHERE m.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED') AND (:building_id IS NULL OR m.building_id = :building_id)) AS open_maintenance,
                    (SELECT COUNT(*) FROM move_requests m WHERE m.status NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED') AND (:building_id IS NULL OR m.building_id = :building_id)) AS active_move_requests
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("building_id", buildingId);
        return jdbc.queryForObject(sql, params, (rs, rowNum) -> {
            long activeUnits = rs.getLong("active_units");
            long occupiedUnits = rs.getLong("occupied_units");
            BigDecimal occupancyRate = activeUnits == 0
                    ? BigDecimal.ZERO.setScale(1)
                    : BigDecimal.valueOf(occupiedUnits * 100.0 / activeUnits).setScale(1, RoundingMode.HALF_UP);

            return new KpiSummary(
                    rs.getLong("buildings"),
                    activeUnits,
                    occupiedUnits,
                    occupancyRate,
                    rs.getLong("active_residents"),
                    rs.getLong("access_events_30d"),
                    rs.getLong("pending_deliveries"),
                    rs.getLong("upcoming_bookings_30d"),
                    rs.getLong("open_incidents"),
                    rs.getLong("open_maintenance"),
                    rs.getLong("active_move_requests")
            );
        });
    }

    public List<StatusMetric> loadStatusMetrics(UUID buildingId) {
        String sql = """
                SELECT category, status, count
                FROM (
                    SELECT 'BOOKINGS' AS category, b.status::text AS status, COUNT(*) AS count
                    FROM bookings b
                    WHERE (:building_id IS NULL OR b.building_id = :building_id)
                    GROUP BY b.status

                    UNION ALL
                    SELECT 'DELIVERIES', d.status::text, COUNT(*)
                    FROM deliveries d
                    WHERE (:building_id IS NULL OR d.building_id = :building_id)
                    GROUP BY d.status

                    UNION ALL
                    SELECT 'INCIDENTS', i.status::text, COUNT(*)
                    FROM incidents i
                    WHERE (:building_id IS NULL OR i.building_id = :building_id)
                    GROUP BY i.status

                    UNION ALL
                    SELECT 'MAINTENANCE', m.status::text, COUNT(*)
                    FROM maintenance_requests m
                    WHERE (:building_id IS NULL OR m.building_id = :building_id)
                    GROUP BY m.status

                    UNION ALL
                    SELECT 'MOVES', m.status::text, COUNT(*)
                    FROM move_requests m
                    WHERE (:building_id IS NULL OR m.building_id = :building_id)
                    GROUP BY m.status
                ) summary
                ORDER BY category, status
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("building_id", buildingId);
        return jdbc.query(sql, params, (rs, rowNum) -> new StatusMetric(
                rs.getString("category"),
                rs.getString("status"),
                rs.getLong("count")
        ));
    }

    public List<MonthlyTrendPoint> loadMonthlyTrend(UUID buildingId) {
        String sql = """
                WITH months AS (
                    SELECT generate_series(
                        date_trunc('month', CURRENT_TIMESTAMP) - INTERVAL '5 months',
                        date_trunc('month', CURRENT_TIMESTAMP),
                        INTERVAL '1 month'
                    ) AS month_start
                ),
                access_counts AS (
                    SELECT date_trunc('month', a.occurred_at) AS month_start, COUNT(*) AS count
                    FROM access_logs a
                    WHERE a.occurred_at >= date_trunc('month', CURRENT_TIMESTAMP) - INTERVAL '5 months'
                      AND (:building_id IS NULL OR a.building_id = :building_id)
                    GROUP BY 1
                ),
                delivery_counts AS (
                    SELECT date_trunc('month', d.received_at) AS month_start, COUNT(*) AS count
                    FROM deliveries d
                    WHERE d.received_at >= date_trunc('month', CURRENT_TIMESTAMP) - INTERVAL '5 months'
                      AND (:building_id IS NULL OR d.building_id = :building_id)
                    GROUP BY 1
                ),
                booking_counts AS (
                    SELECT date_trunc('month', b.start_at) AS month_start, COUNT(*) AS count
                    FROM bookings b
                    WHERE b.start_at >= date_trunc('month', CURRENT_TIMESTAMP) - INTERVAL '5 months'
                      AND (:building_id IS NULL OR b.building_id = :building_id)
                    GROUP BY 1
                ),
                incident_counts AS (
                    SELECT date_trunc('month', i.created_at) AS month_start, COUNT(*) AS count
                    FROM incidents i
                    WHERE i.created_at >= date_trunc('month', CURRENT_TIMESTAMP) - INTERVAL '5 months'
                      AND (:building_id IS NULL OR i.building_id = :building_id)
                    GROUP BY 1
                ),
                maintenance_counts AS (
                    SELECT date_trunc('month', m.created_at) AS month_start, COUNT(*) AS count
                    FROM maintenance_requests m
                    WHERE m.created_at >= date_trunc('month', CURRENT_TIMESTAMP) - INTERVAL '5 months'
                      AND (:building_id IS NULL OR m.building_id = :building_id)
                    GROUP BY 1
                ),
                move_counts AS (
                    SELECT date_trunc('month', m.requested_at) AS month_start, COUNT(*) AS count
                    FROM move_requests m
                    WHERE m.requested_at >= date_trunc('month', CURRENT_TIMESTAMP) - INTERVAL '5 months'
                      AND (:building_id IS NULL OR m.building_id = :building_id)
                    GROUP BY 1
                )
                SELECT
                    to_char(months.month_start, 'YYYY-MM') AS month,
                    COALESCE(access_counts.count, 0) AS access_events,
                    COALESCE(delivery_counts.count, 0) AS deliveries,
                    COALESCE(booking_counts.count, 0) AS bookings,
                    COALESCE(incident_counts.count, 0) AS incidents,
                    COALESCE(maintenance_counts.count, 0) AS maintenance,
                    COALESCE(move_counts.count, 0) AS moves
                FROM months
                LEFT JOIN access_counts ON access_counts.month_start = months.month_start
                LEFT JOIN delivery_counts ON delivery_counts.month_start = months.month_start
                LEFT JOIN booking_counts ON booking_counts.month_start = months.month_start
                LEFT JOIN incident_counts ON incident_counts.month_start = months.month_start
                LEFT JOIN maintenance_counts ON maintenance_counts.month_start = months.month_start
                LEFT JOIN move_counts ON move_counts.month_start = months.month_start
                ORDER BY months.month_start
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("building_id", buildingId);
        return jdbc.query(sql, params, (rs, rowNum) -> new MonthlyTrendPoint(
                rs.getString("month"),
                rs.getLong("access_events"),
                rs.getLong("deliveries"),
                rs.getLong("bookings"),
                rs.getLong("incidents"),
                rs.getLong("maintenance"),
                rs.getLong("moves")
        ));
    }

    public List<BuildingReportSummary> loadBuildings(UUID buildingId) {
        String sql = """
                WITH unit_counts AS (
                    SELECT
                        u.building_id,
                        COUNT(*) FILTER (WHERE u.active = true) AS active_units,
                        COUNT(DISTINCT r.unit_id) FILTER (WHERE r.active = true AND u.active = true) AS occupied_units,
                        COUNT(DISTINCT r.id) FILTER (WHERE r.active = true AND u.active = true) AS active_residents
                    FROM units u
                    LEFT JOIN residents r ON r.unit_id = u.id
                    GROUP BY u.building_id
                ),
                incident_counts AS (
                    SELECT building_id, COUNT(*) FILTER (WHERE status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')) AS open_incidents
                    FROM incidents
                    GROUP BY building_id
                ),
                maintenance_counts AS (
                    SELECT building_id, COUNT(*) FILTER (WHERE status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')) AS open_maintenance
                    FROM maintenance_requests
                    GROUP BY building_id
                ),
                delivery_counts AS (
                    SELECT building_id, COUNT(*) FILTER (WHERE status IN ('RECEIVED', 'NOTIFIED')) AS pending_deliveries
                    FROM deliveries
                    GROUP BY building_id
                )
                SELECT
                    b.id AS building_id,
                    b.code,
                    b.name,
                    COALESCE(unit_counts.active_units, 0) AS active_units,
                    COALESCE(unit_counts.occupied_units, 0) AS occupied_units,
                    COALESCE(unit_counts.active_residents, 0) AS active_residents,
                    COALESCE(incident_counts.open_incidents, 0) AS open_incidents,
                    COALESCE(maintenance_counts.open_maintenance, 0) AS open_maintenance,
                    COALESCE(delivery_counts.pending_deliveries, 0) AS pending_deliveries
                FROM buildings b
                LEFT JOIN unit_counts ON unit_counts.building_id = b.id
                LEFT JOIN incident_counts ON incident_counts.building_id = b.id
                LEFT JOIN maintenance_counts ON maintenance_counts.building_id = b.id
                LEFT JOIN delivery_counts ON delivery_counts.building_id = b.id
                WHERE b.active = true
                  AND (:building_id IS NULL OR b.id = :building_id)
                ORDER BY b.name
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("building_id", buildingId);
        return jdbc.query(sql, params, (rs, rowNum) -> {
            long activeUnits = rs.getLong("active_units");
            long occupiedUnits = rs.getLong("occupied_units");
            BigDecimal occupancyRate = activeUnits == 0
                    ? BigDecimal.ZERO.setScale(1)
                    : BigDecimal.valueOf(occupiedUnits * 100.0 / activeUnits).setScale(1, RoundingMode.HALF_UP);

            return new BuildingReportSummary(
                    rs.getObject("building_id", UUID.class),
                    rs.getString("code"),
                    rs.getString("name"),
                    activeUnits,
                    occupiedUnits,
                    occupancyRate,
                    rs.getLong("active_residents"),
                    rs.getLong("open_incidents"),
                    rs.getLong("open_maintenance"),
                    rs.getLong("pending_deliveries")
            );
        });
    }
}
