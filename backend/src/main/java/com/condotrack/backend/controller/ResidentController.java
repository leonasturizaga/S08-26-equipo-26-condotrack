package com.condotrack.backend.controller;

import com.condotrack.backend.dto.ResidentResponse;
import com.condotrack.backend.dto.ResidentUpdateRequest;
import com.condotrack.backend.service.ResidentAccessService;
import com.condotrack.backend.service.ResidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentAccessService residentAccessService;
    private final ResidentService residentService;

    @GetMapping("/{residentId}")
    @PreAuthorize("@residentAccessService.canView(authentication, #residentId)")
    public ResidentResponse getResident(@PathVariable UUID residentId) {
        return residentService.getResident(residentId);
    }

    @PutMapping("/{residentId}")
    @PreAuthorize("@residentAccessService.canUpdate(authentication, #residentId)")
    public ResidentResponse updateResident(
            @PathVariable UUID residentId,
            @Valid @RequestBody ResidentUpdateRequest request
    ) {
        return residentService.updateResident(residentId, request);
    }
}
