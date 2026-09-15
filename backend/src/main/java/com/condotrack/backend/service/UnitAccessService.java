package com.condotrack.backend.service;

import com.condotrack.backend.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("unitAccessService")
@RequiredArgsConstructor
public class UnitAccessService {

    private final PermissionService permissionService;
    private final ResidentRepository residentRepository;

    @Transactional(readOnly = true)
    public boolean canView(Authentication authentication, UUID unitId) {
        if (authentication == null || !authentication.isAuthenticated() || unitId == null) {
            return false;
        }

        // Global permission: administrators and reception can view all units.
        if (permissionService.hasPermission(authentication, "UNITS_VIEW")) {
            return true;
        }

        // Scoped permission: resident/owner may only view a unit to which
        // the authenticated user has an active resident relationship.
        if (!permissionService.hasPermission(authentication, "UNITS_VIEW_OWN")) {
            return false;
        }

        return residentRepository.existsActiveResidentForUserAndUnit(
                authentication.getName(),
                unitId
        );
    }
}
