package com.condotrack.backend.controller;

import com.condotrack.backend.dto.BuildingResponse;
import com.condotrack.backend.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public List<BuildingResponse> getAllBuildings() {
        return buildingService.getAllBuildings();
    }
}
