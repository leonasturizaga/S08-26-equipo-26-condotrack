//----------------------- M16 ----------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.repository.MaintenanceRequestRepository;
// import com.condotrack.backend.repository.ResidentRepository;
// import com.condotrack.backend.repository.StaffRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;

// import java.util.UUID;

// @Service("maintenanceAccessService")
// @RequiredArgsConstructor
// public class MaintenanceAccessService {
//     private final PermissionService permissionService;
//     private final MaintenanceRequestRepository maintenanceRepository;
//     private final ResidentRepository residentRepository;
//     private final StaffRepository staffRepository;

//     public boolean canList(Authentication authentication) {
//         if (authentication == null || !authentication.isAuthenticated()) return false;
//         return permissionService.hasPermission(authentication, "MAINTENANCE_VIEW")
//                 || permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_ASSIGNED")
//                 || permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_OWN")
//                 || permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_UNIT");
//     }

//     public boolean canView(Authentication authentication, UUID id) {
//         if (authentication == null || !authentication.isAuthenticated() || id == null) return false;
//         if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW") && maintenanceRepository.existsById(id)) return true;
//         if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_ASSIGNED")
//                 && maintenanceRepository.existsByIdAndAssignedToStaff_User_EmailIgnoreCase(id, authentication.getName())) return true;
//         if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_OWN")
//                 && maintenanceRepository.existsByIdAndRequestedByUser_EmailIgnoreCase(id, authentication.getName())) return true;
//         return permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_UNIT")
//                 && maintenanceRepository.existsForOwnedUnit(id, authentication.getName());
//     }

//     public boolean canCreate(Authentication authentication, UUID unitId) {
//         if (authentication == null || !authentication.isAuthenticated() || unitId == null) return false;
//         if (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE_OWN")) {
//             return residentRepository.existsActiveResidentForUserAndUnit(authentication.getName(), unitId);
//         }
//         return permissionService.hasPermission(authentication, "MAINTENANCE_CREATE");
//     }

//     public boolean canCreateOptions(Authentication authentication) {
//         return authentication != null && authentication.isAuthenticated()
//                 && (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE")
//                     || permissionService.hasPermission(authentication, "MAINTENANCE_CREATE_OWN"));
//     }

//     public boolean canUpdate(Authentication authentication, UUID id) {
//         if (authentication == null || !authentication.isAuthenticated() || id == null) return false;
//         if (permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE") && maintenanceRepository.existsById(id)) return true;
//         return permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE_OWN")
//                 && maintenanceRepository.existsByIdAndRequestedByUser_EmailIgnoreCase(id, authentication.getName());
//     }

//     public boolean canAssign(Authentication authentication) {
//         return authentication != null && authentication.isAuthenticated()
//                 && permissionService.hasPermission(authentication, "MAINTENANCE_ASSIGN");
//     }

//     public boolean canUpdateStatus(Authentication authentication, UUID id) {
//         if (authentication == null || !authentication.isAuthenticated() || id == null) return false;
//         if (permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE") && maintenanceRepository.existsById(id)) return true;
//         return permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE_ASSIGNED")
//                 && maintenanceRepository.existsByIdAndAssignedToStaff_User_EmailIgnoreCase(id, authentication.getName());
//     }

// }


//----------------------- M17.1 ----------------
package com.condotrack.backend.service;

import com.condotrack.backend.repository.MaintenanceRequestRepository;
import com.condotrack.backend.repository.ResidentRepository;
import com.condotrack.backend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("maintenanceAccessService")
@RequiredArgsConstructor
public class MaintenanceAccessService {
    private final PermissionService permissionService;
    private final MaintenanceRequestRepository maintenanceRepository;
    private final ResidentRepository residentRepository;
    private final StaffRepository staffRepository;

    @Transactional(readOnly = true)
    public boolean canList(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;

        if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW")) {
            if (hasRole(authentication, "ADMINISTRATOR")) return true;

            return staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
                    .map(staff -> staff.getBuilding().getId())
                    .distinct()
                    .anyMatch(buildingId -> permissionService.hasPermission(authentication, "MAINTENANCE_VIEW", buildingId));
        }

        return permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_ASSIGNED")
                || permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_OWN")
                || permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_UNIT");
    }

    @Transactional(readOnly = true)
    public boolean canView(Authentication authentication, UUID id) {
        if (authentication == null || !authentication.isAuthenticated() || id == null) return false;

        UUID buildingId = maintenanceRepository.findBuildingIdById(id).orElse(null);
        if (buildingId == null) return false;

        if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW", buildingId)
                && maintenanceRepository.existsById(id)) return true;
        if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_ASSIGNED", buildingId)
                && maintenanceRepository.existsByIdAndAssignedToStaff_User_EmailIgnoreCase(id, authentication.getName())) return true;
        if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_OWN", buildingId)
                && maintenanceRepository.existsByIdAndRequestedByUser_EmailIgnoreCase(id, authentication.getName())) return true;
        return permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_UNIT", buildingId)
                && maintenanceRepository.existsForOwnedUnit(id, authentication.getName());
    }

    @Transactional(readOnly = true)
    public boolean canCreate(Authentication authentication, UUID unitId) {
        if (authentication == null || !authentication.isAuthenticated() || unitId == null) return false;
        UUID buildingId = maintenanceRepository.findBuildingIdByUnitId(unitId).orElse(null);
        if (buildingId == null) return false;

        if (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE_OWN", buildingId)) {
            return residentRepository.existsActiveResidentForUserAndUnit(authentication.getName(), unitId);
        }
        return permissionService.hasPermission(authentication, "MAINTENANCE_CREATE", buildingId);
    }

    public boolean canCreateOptions(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE")
                    || permissionService.hasPermission(authentication, "MAINTENANCE_CREATE_OWN"));
    }

    @Transactional(readOnly = true)
    public boolean canUpdate(Authentication authentication, UUID id) {
        if (authentication == null || !authentication.isAuthenticated() || id == null) return false;
        UUID buildingId = maintenanceRepository.findBuildingIdById(id).orElse(null);
        if (buildingId == null) return false;
        if (permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE", buildingId)
                && maintenanceRepository.existsById(id)) return true;
        return permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE_OWN", buildingId)
                && maintenanceRepository.existsByIdAndRequestedByUser_EmailIgnoreCase(id, authentication.getName());
    }

    public boolean canAssign(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && permissionService.hasPermission(authentication, "MAINTENANCE_ASSIGN");
    }

    @Transactional(readOnly = true)
    public boolean canUpdateStatus(Authentication authentication, UUID id) {
        if (authentication == null || !authentication.isAuthenticated() || id == null) return false;
        UUID buildingId = maintenanceRepository.findBuildingIdById(id).orElse(null);
        if (buildingId == null) return false;
        if (permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE", buildingId)
                && maintenanceRepository.existsById(id)) return true;
        return permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE_ASSIGNED", buildingId)
                && maintenanceRepository.existsByIdAndAssignedToStaff_User_EmailIgnoreCase(id, authentication.getName());
    }

    private boolean hasRole(Authentication authentication, String roleCode) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ("ROLE_" + roleCode).equals(authority.getAuthority()));
    }
}
