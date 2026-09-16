package com.condotrack.backend.controller;

import com.condotrack.backend.dto.AccessLogResponse;
import com.condotrack.backend.dto.VisitorAuthorizationCreateRequest;
import com.condotrack.backend.dto.VisitorAuthorizationResponse;
import com.condotrack.backend.service.AccessAccessService;
import com.condotrack.backend.service.AccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/access")
@RequiredArgsConstructor
public class AccessController {

    private final AccessService accessService;
    private final AccessAccessService accessAccessService;

    @PostMapping("/visitor-authorizations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@accessAccessService.canCreateVisitorAuthorization(authentication, #request.unitId, #request.residentId)")
    public VisitorAuthorizationResponse createVisitorAuthorization(
            @Valid @RequestBody VisitorAuthorizationCreateRequest request,
            Authentication authentication
    ) {
        return accessService.createAuthorization(request, authentication);
    }

    @PostMapping("/visitor-authorizations/{authorizationId}/check-in")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'ACCESS_CREATE')")
    public AccessLogResponse checkIn(
            @PathVariable UUID authorizationId,
            Authentication authentication
    ) {
        return accessService.checkIn(authorizationId, authentication);
    }
}
