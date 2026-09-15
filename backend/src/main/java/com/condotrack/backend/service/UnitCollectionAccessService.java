package com.condotrack.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("unitCollectionAccessService")
@RequiredArgsConstructor
public class UnitCollectionAccessService {

    private final PermissionService permissionService;

    public boolean canViewCollection(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return permissionService.hasPermission(authentication, "UNITS_VIEW")
                || permissionService.hasPermission(authentication, "UNITS_VIEW_OWN");
    }
}
