package com.condotrack.backend.service;

import com.condotrack.backend.repository.MaintenanceRequestRepository;
import com.condotrack.backend.repository.ResidentRepository;
import com.condotrack.backend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("maintenanceAccessService")
@RequiredArgsConstructor
public class MaintenanceAccessService {
    private final PermissionService permissionService;
    private final MaintenanceRequestRepository maintenanceRepository;
    private final ResidentRepository residentRepository;
    private final StaffRepository staffRepository;

    public boolean canList(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        return permissionService.hasPermission(authentication, "MAINTENANCE_VIEW")
                || permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_ASSIGNED")
                || permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_OWN")
                || permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_UNIT");
    }

    public boolean canView(Authentication authentication, UUID id) {
        if (authentication == null || !authentication.isAuthenticated() || id == null) return false;
        if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW") && maintenanceRepository.existsById(id)) return true;
        if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_ASSIGNED")
                && maintenanceRepository.existsByIdAndAssignedToStaff_User_EmailIgnoreCase(id, authentication.getName())) return true;
        if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_OWN")
                && maintenanceRepository.existsByIdAndRequestedByUser_EmailIgnoreCase(id, authentication.getName())) return true;
        return permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_UNIT")
                && maintenanceRepository.existsForOwnedUnit(id, authentication.getName());
    }

    public boolean canCreate(Authentication authentication, UUID unitId) {
        if (authentication == null || !authentication.isAuthenticated() || unitId == null) return false;
        if (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE_OWN")) {
            return residentRepository.existsActiveResidentForUserAndUnit(authentication.getName(), unitId);
        }
        return permissionService.hasPermission(authentication, "MAINTENANCE_CREATE");
    }

    public boolean canCreateOptions(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE")
                    || permissionService.hasPermission(authentication, "MAINTENANCE_CREATE_OWN"));
    }

    public boolean canUpdate(Authentication authentication, UUID id) {
        if (authentication == null || !authentication.isAuthenticated() || id == null) return false;
        if (permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE") && maintenanceRepository.existsById(id)) return true;
        return permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE_OWN")
                && maintenanceRepository.existsByIdAndRequestedByUser_EmailIgnoreCase(id, authentication.getName());
    }

    public boolean canAssign(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && permissionService.hasPermission(authentication, "MAINTENANCE_ASSIGN");
    }

    public boolean canUpdateStatus(Authentication authentication, UUID id) {
        if (authentication == null || !authentication.isAuthenticated() || id == null) return false;
        if (permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE") && maintenanceRepository.existsById(id)) return true;
        return permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE_ASSIGNED")
                && maintenanceRepository.existsByIdAndAssignedToStaff_User_EmailIgnoreCase(id, authentication.getName());
    }

}
