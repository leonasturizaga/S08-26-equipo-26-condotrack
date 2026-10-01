//------------------------------- M24.8.3 ---------------------------
package com.condotrack.backend.media.service;

import com.condotrack.backend.media.model.MediaEntityType;
import com.condotrack.backend.repository.IncidentRepository;
import com.condotrack.backend.repository.MaintenanceRequestRepository;
import com.condotrack.backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("mediaAccessService")
@RequiredArgsConstructor
public class MediaAccessService {

    private final PermissionService permissionService;
    private final MaintenanceRequestRepository maintenanceRepository;
    private final IncidentRepository incidentRepository;

    @Transactional(readOnly = true)
    public boolean canView(
            Authentication authentication,
            MediaEntityType entityType,
            UUID entityId
    ) {
        if (!authenticated(authentication) || entityType == null || entityId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "BUILDING_CONFIG_VIEW")) {
            return true;
        }

        return switch (entityType) {
            case MAINTENANCE -> isAssignedToMaintenance(authentication, entityId);
            case INCIDENT -> isAssignedToIncident(authentication, entityId);
            default -> false;
        };
    }

    @Transactional(readOnly = true)
    public boolean canUpload(
            Authentication authentication,
            MediaEntityType entityType,
            UUID entityId
    ) {
        if (!authenticated(authentication) || entityType == null || entityId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "BUILDING_CONFIG_UPDATE")) {
            return true;
        }

        return switch (entityType) {
            case MAINTENANCE -> isAssignedToMaintenance(authentication, entityId);
            case INCIDENT -> isAssignedToIncident(authentication, entityId);
            default -> false;
        };
    }

    public boolean canDelete(
            Authentication authentication,
            MediaEntityType entityType,
            UUID entityId
    ) {
        return authenticated(authentication)
                && entityType != null
                && entityId != null
                && permissionService.hasPermission(
                        authentication,
                        "BUILDING_CONFIG_UPDATE");
    }

    private boolean authenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    private boolean isAssignedToMaintenance(
            Authentication authentication,
            UUID maintenanceId
    ) {
        return maintenanceRepository
                .existsByIdAndAssignedToStaff_User_EmailIgnoreCase(
                        maintenanceId,
                        authentication.getName());
    }

    private boolean isAssignedToIncident(
            Authentication authentication,
            UUID incidentId
    ) {
        return incidentRepository
                .existsByIdAndAssignedToStaff_User_EmailIgnoreCase(
                        incidentId,
                        authentication.getName());
    }
}
