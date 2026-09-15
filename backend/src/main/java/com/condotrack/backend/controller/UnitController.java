package com.condotrack.backend.controller;

import com.condotrack.backend.dto.UnitResponse;
import com.condotrack.backend.service.UnitAccessService;
import com.condotrack.backend.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitAccessService unitAccessService;
    private final UnitService unitService;

    @GetMapping("/{unitId}")
    @PreAuthorize("@unitAccessService.canView(authentication, #unitId)")
    public UnitResponse getUnit(@PathVariable UUID unitId) {
        return unitService.getUnit(unitId);
    }
}
