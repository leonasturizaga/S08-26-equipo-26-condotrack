package com.condotrack.backend.controller;

import com.condotrack.backend.dto.UnitDashboardResponse;
import com.condotrack.backend.service.UnitDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitDashboardController {
    private final UnitDashboardService unitDashboardService;

    @GetMapping("/{unitId}/dashboard")
    @PreAuthorize("@unitDashboardAccessService.canView(authentication, #unitId)")
    public UnitDashboardResponse getDashboard(@PathVariable UUID unitId) {
        return unitDashboardService.getUnitDashboard(unitId);
    }
}
