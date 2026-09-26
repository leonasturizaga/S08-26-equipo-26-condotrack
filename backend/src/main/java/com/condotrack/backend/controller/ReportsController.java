package com.condotrack.backend.controller;

import com.condotrack.backend.dto.ReportsSummaryResponse;
import com.condotrack.backend.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Administrator reports and operational KPIs.")
public class ReportsController {
    private final ReportsService reportsService;

    @GetMapping("/summary")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'REPORTS_VIEW')")
    @Operation(summary = "Get operational KPI summary")
    public ReportsSummaryResponse getSummary(
            @Parameter(description = "Optional active building UUID. When omitted, all active buildings are included.")
            @RequestParam(required = false) UUID buildingId
    ) {
        return reportsService.getSummary(buildingId);
    }
}
