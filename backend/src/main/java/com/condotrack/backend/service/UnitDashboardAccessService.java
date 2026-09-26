package com.condotrack.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("unitDashboardAccessService")
@RequiredArgsConstructor
public class UnitDashboardAccessService {

    private final PermissionService permissionService;

    public boolean canView(Authentication authentication, UUID unitId) {
        if (authentication == null || !authentication.isAuthenticated() || unitId == null) {
            return false;
        }

        // The unified workspace exposes cross-module operational data.
        // Only roles with global unit visibility may access it.
        return permissionService.hasPermission(authentication, "UNITS_VIEW");
    }
}
