package com.condotrack.backend.controller;

import com.condotrack.backend.dto.IncidentAssignmentRequest;
import com.condotrack.backend.dto.IncidentCreateRequest;
import com.condotrack.backend.dto.IncidentPageResponse;
import com.condotrack.backend.dto.IncidentResponse;
import com.condotrack.backend.dto.IncidentStaffResponse;
import com.condotrack.backend.dto.IncidentStatusUpdateRequest;
import com.condotrack.backend.dto.IncidentUnitOptionResponse;
import com.condotrack.backend.dto.IncidentUpdateRequest;
import com.condotrack.backend.service.IncidentAccessService;
import com.condotrack.backend.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;
    private final IncidentAccessService incidentAccessService;

    @GetMapping
    @PreAuthorize("@incidentAccessService.canList(authentication)")
    public IncidentPageResponse getIncidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return incidentService.getIncidents(authentication, page, size);
    }

    @GetMapping("/{incidentId}")
    @PreAuthorize("@incidentAccessService.canView(authentication, #incidentId)")
    public IncidentResponse getIncident(
            @PathVariable UUID incidentId
    ) {
        return incidentService.getIncident(incidentId);
    }

    @GetMapping("/options/units")
    @PreAuthorize("@incidentAccessService.canCreateOptions(authentication)")
    public List<IncidentUnitOptionResponse> getIncidentUnitOptions(Authentication authentication) {
        return incidentService.getIncidentUnitOptions(authentication);
    }

    @GetMapping("/assignable-staff")
    @PreAuthorize("@incidentAccessService.canAssign(authentication)")
    public List<IncidentStaffResponse> getAssignableStaff(
            @RequestParam UUID buildingId
    ) {
        return incidentService.getAssignableStaff(buildingId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@incidentAccessService.canCreate(authentication, #request.unitId)")
    public IncidentResponse createIncident(
            @Valid @RequestBody IncidentCreateRequest request,
            Authentication authentication
    ) {
        return incidentService.createIncident(request, authentication);
    }

    @PutMapping("/{incidentId}")
    @PreAuthorize("@incidentAccessService.canUpdate(authentication, #incidentId)")
    public IncidentResponse updateIncident(
            @PathVariable UUID incidentId,
            @Valid @RequestBody IncidentUpdateRequest request,
            Authentication authentication
    ) {
        return incidentService.updateIncident(incidentId, request, authentication);
    }

    @PutMapping("/{incidentId}/assignment")
    @PreAuthorize("@incidentAccessService.canAssign(authentication)")
    public IncidentResponse assignIncident(
            @PathVariable UUID incidentId,
            @RequestBody IncidentAssignmentRequest request,
            Authentication authentication
    ) {
        return incidentService.assignIncident(incidentId, request, authentication);
    }

    @PutMapping("/{incidentId}/status")
    @PreAuthorize("@incidentAccessService.canUpdateStatus(authentication, #incidentId)")
    public IncidentResponse updateStatus(
            @PathVariable UUID incidentId,
            @Valid @RequestBody IncidentStatusUpdateRequest request,
            Authentication authentication
    ) {
        return incidentService.updateStatus(incidentId, request, authentication);
    }
}
