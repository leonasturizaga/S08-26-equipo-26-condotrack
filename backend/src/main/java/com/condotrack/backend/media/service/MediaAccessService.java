package com.condotrack.backend.media.service;

import com.condotrack.backend.media.model.MediaEntityType;
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

    /**
     * Determines whether the authenticated user may view media attached
     * to the specified entity.
     *
     * For Maintenance:
     * - existing BUILDING_CONFIG_VIEW permission remains valid
     * - an assigned Provider may view the maintenance media
     *
     * Other entity types retain the existing BUILDING_CONFIG_VIEW rule.
     */
    @Transactional(readOnly = true)
    public boolean canView(
            Authentication authentication,
            MediaEntityType entityType,
            UUID entityId
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || entityType == null
                || entityId == null) {
            return false;
        }

        if (permissionService.hasPermission(
                authentication,
                "BUILDING_CONFIG_VIEW")) {
            return true;
        }

        if (entityType == MediaEntityType.MAINTENANCE) {
            return isAssignedToMaintenance(authentication, entityId);
        }

        return false;
    }

    /**
     * Determines whether the authenticated user may upload media
     * to the specified entity.
     *
     * For Maintenance:
     * - existing BUILDING_CONFIG_UPDATE permission remains valid
     * - an assigned Provider may upload
     *
     * Providers do not receive BUILDING_CONFIG_UPDATE merely because
     * they are allowed to upload maintenance media.
     */
    @Transactional(readOnly = true)
    public boolean canUpload(
            Authentication authentication,
            MediaEntityType entityType,
            UUID entityId
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || entityType == null
                || entityId == null) {
            return false;
        }

        if (permissionService.hasPermission(
                authentication,
                "BUILDING_CONFIG_UPDATE")) {
            return true;
        }

        if (entityType == MediaEntityType.MAINTENANCE) {
            return isAssignedToMaintenance(authentication, entityId);
        }

        return false;
    }

    /**
     * Media deletion remains restricted to the existing
     * BUILDING_CONFIG_UPDATE permission.
     *
     * This intentionally does not grant Providers delete access.
     */
    public boolean canDelete(
            Authentication authentication,
            MediaEntityType entityType,
            UUID entityId
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || entityType == null
                || entityId == null) {
            return false;
        }

        return permissionService.hasPermission(
                authentication,
                "BUILDING_CONFIG_UPDATE");
    }

    private boolean isAssignedToMaintenance(
            Authentication authentication,
            UUID maintenanceId
    ) {
        return maintenanceRepository
                .existsByIdAndAssignedToStaff_User_EmailIgnoreCase(
                        maintenanceId,
                        authentication.getName()
                );
    }
}