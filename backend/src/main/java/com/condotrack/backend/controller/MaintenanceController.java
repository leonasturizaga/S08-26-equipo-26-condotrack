package com.condotrack.backend.controller;

import com.condotrack.backend.dto.*;
import com.condotrack.backend.service.MaintenanceAccessService;
import com.condotrack.backend.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {
    private final MaintenanceService maintenanceService;
    private final MaintenanceAccessService maintenanceAccessService;

    @GetMapping
    @PreAuthorize("@maintenanceAccessService.canList(authentication)")
    public MaintenancePageResponse getMaintenance(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Authentication authentication) {
        return maintenanceService.getMaintenance(authentication, page, size);
    }

    @GetMapping("/{maintenanceId}")
    @PreAuthorize("@maintenanceAccessService.canView(authentication, #maintenanceId)")
    public MaintenanceResponse getMaintenance(@PathVariable UUID maintenanceId) { return maintenanceService.getMaintenance(maintenanceId); }

    @GetMapping("/options/units")
    @PreAuthorize("@maintenanceAccessService.canCreateOptions(authentication)")
    public List<MaintenanceUnitOptionResponse> getUnitOptions(Authentication authentication) { return maintenanceService.getUnitOptions(authentication); }

    @GetMapping("/assignable-staff")
    @PreAuthorize("@maintenanceAccessService.canAssign(authentication)")
    public List<MaintenanceStaffResponse> getAssignableStaff(@RequestParam UUID buildingId) { return maintenanceService.getAssignableStaff(buildingId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@maintenanceAccessService.canCreate(authentication, #request.unitId)")
    public MaintenanceResponse createMaintenance(@Valid @RequestBody MaintenanceCreateRequest request, Authentication authentication) { return maintenanceService.createMaintenance(request, authentication); }

    @PutMapping("/{maintenanceId}")
    @PreAuthorize("@maintenanceAccessService.canUpdate(authentication, #maintenanceId)")
    public MaintenanceResponse updateMaintenance(@PathVariable UUID maintenanceId, @Valid @RequestBody MaintenanceUpdateRequest request, Authentication authentication) { return maintenanceService.updateMaintenance(maintenanceId, request, authentication); }

    @PutMapping("/{maintenanceId}/assignment")
    @PreAuthorize("@maintenanceAccessService.canAssign(authentication)")
    public MaintenanceResponse assignMaintenance(@PathVariable UUID maintenanceId, @RequestBody MaintenanceAssignmentRequest request, Authentication authentication) { return maintenanceService.assignMaintenance(maintenanceId, request, authentication); }

    @PutMapping("/{maintenanceId}/status")
    @PreAuthorize("@maintenanceAccessService.canUpdateStatus(authentication, #maintenanceId)")
    public MaintenanceResponse updateStatus(@PathVariable UUID maintenanceId, @Valid @RequestBody MaintenanceStatusUpdateRequest request, Authentication authentication) { return maintenanceService.updateStatus(maintenanceId, request, authentication); }
}
