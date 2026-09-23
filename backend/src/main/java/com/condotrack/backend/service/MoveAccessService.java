//-------------------- Milestone 18.3 rev3 ----------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.MoveCreateRequest;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.repository.MoveRequestRepository;
import com.condotrack.backend.repository.ResidentRepository;
import com.condotrack.backend.repository.StaffRepository;
import com.condotrack.backend.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("moveAccessService")
@RequiredArgsConstructor
public class MoveAccessService {
    private final PermissionService permissionService;
    private final MoveRequestRepository moveRequestRepository;
    private final ResidentRepository residentRepository;
    private final StaffRepository staffRepository;
    private final UnitRepository unitRepository;

    @Transactional(readOnly = true)
    public boolean canList(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (hasRole(authentication, "ADMINISTRATOR") && permissionService.hasPermission(authentication, "MOVES_VIEW")) return true;
        if (permissionService.hasPermission(authentication, "MOVES_VIEW")) {
            return staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
                    .map(staff -> staff.getBuilding().getId())
                    .distinct()
                    .anyMatch(buildingId -> permissionService.hasPermission(authentication, "MOVES_VIEW", buildingId));
        }
        return permissionService.hasPermission(authentication, "MOVES_VIEW_OWN")
                || permissionService.hasPermission(authentication, "MOVES_VIEW_UNIT");
    }

    @Transactional(readOnly = true)
    public boolean canView(Authentication authentication, UUID id) {
        if (authentication == null || !authentication.isAuthenticated() || id == null) return false;
        UUID buildingId = moveRequestRepository.findBuildingIdById(id).orElse(null);
        if (buildingId == null) return false;
        if (permissionService.hasPermission(authentication, "MOVES_VIEW", buildingId)) return true;
        if (permissionService.hasPermission(authentication, "MOVES_VIEW_OWN", buildingId)
                && moveRequestRepository.existsByIdAndResident_User_EmailIgnoreCase(id, authentication.getName())) return true;
        UUID unitId = moveRequestRepository.findUnitIdById(id).orElse(null);
        return unitId != null && permissionService.hasPermission(authentication, "MOVES_VIEW_UNIT", buildingId)
                && residentRepository.existsActiveOwnerForUserAndUnit(authentication.getName(), unitId);
    }

    @Transactional(readOnly = true)
    public boolean canCreate(Authentication authentication, MoveCreateRequest request) {
        if (authentication == null || !authentication.isAuthenticated() || request == null || request.unitId() == null) {
            return false;
        }

        UUID unitId = request.unitId();

        // Administrators are global and can create moves for any active unit.
        if (hasRole(authentication, "ADMINISTRATOR")) {
            return permissionService.hasPermission(authentication, "MOVES_CREATE");
        }

        // Residents may create requests only for their own active unit.
        if (permissionService.hasPermission(authentication, "MOVES_CREATE_OWN")) {
            return residentRepository.existsActiveResidentForUserAndUnit(authentication.getName(), unitId);
        }

        Unit unit = unitRepository.findById(unitId).orElse(null);
        if (unit == null || unit.getBuilding() == null) {
            return false;
        }

        UUID buildingId = unit.getBuilding().getId();

        // Reception/staff users must have the global permission, the effective
        // building permission, and an active staff assignment for that building.
        if (!permissionService.hasPermission(authentication, "MOVES_CREATE", buildingId)) {
            return false;
        }

        return staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
                .anyMatch(staff -> staff.getBuilding() != null
                        && staff.getBuilding().getId().equals(buildingId));
    }

    public boolean canCreateOptions(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && (permissionService.hasPermission(authentication, "MOVES_CREATE")
                || permissionService.hasPermission(authentication, "MOVES_CREATE_OWN"));
    }

    public boolean canApprove(Authentication authentication, UUID id) {
        if (authentication == null || !authentication.isAuthenticated() || id == null) return false;
        UUID buildingId = moveRequestRepository.findBuildingIdById(id).orElse(null);
        return buildingId != null && permissionService.hasPermission(authentication, "MOVES_APPROVE", buildingId)
                && hasRole(authentication, "ADMINISTRATOR");
    }

    @Transactional(readOnly = true)
    public boolean canAuthorizeOwner(Authentication authentication, UUID id) {
        if (authentication == null || !authentication.isAuthenticated() || id == null) return false;
        UUID buildingId = moveRequestRepository.findBuildingIdById(id).orElse(null);
        UUID unitId = moveRequestRepository.findUnitIdById(id).orElse(null);
        return buildingId != null && unitId != null
                && permissionService.hasPermission(authentication, "MOVES_AUTHORIZE", buildingId)
                && residentRepository.existsActiveOwnerForUserAndUnit(authentication.getName(), unitId);
    }

    public boolean canUpdateStatus(Authentication authentication, UUID id) {
        return canApprove(authentication, id);
    }

    private boolean hasRole(Authentication authentication, String roleCode) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> ("ROLE_" + roleCode).equals(a.getAuthority()));
    }
}
