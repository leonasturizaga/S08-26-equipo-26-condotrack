package com.condotrack.backend.service;

import com.condotrack.backend.dto.ReportsSummaryResponse;
import com.condotrack.backend.repository.BuildingRepository;
import com.condotrack.backend.repository.ReportsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportsService {
    private final ReportsRepository reportsRepository;
    private final BuildingRepository buildingRepository;

    @Transactional(readOnly = true)
    public ReportsSummaryResponse getSummary(UUID buildingId) {
        if (buildingId != null) {
            buildingRepository.findByIdAndActiveTrue(buildingId)
                    .orElseThrow(() -> new IllegalArgumentException("Building not found: " + buildingId));
        }

        return new ReportsSummaryResponse(
                OffsetDateTime.now(),
                buildingId,
                reportsRepository.loadKpis(buildingId),
                reportsRepository.loadStatusMetrics(buildingId),
                reportsRepository.loadMonthlyTrend(buildingId),
                reportsRepository.loadBuildings(buildingId)
        );
    }
}
