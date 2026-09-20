package com.condotrack.backend.service;

import com.condotrack.backend.repository.IncidentRepository;
import com.condotrack.backend.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("incidentAccessService")
@RequiredArgsConstructor
public class IncidentAccessService {

    private final PermissionService permissionService;
    private final IncidentRepository incidentRepository;
    private final ResidentRepository residentRepository;

    public boolean canList(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return permissionService.hasPermission(authentication, "INCIDENTS_VIEW")
                || permissionService.hasPermission(authentication, "INCIDENTS_VIEW_OWN")
                || permissionService.hasPermission(authentication, "INCIDENTS_VIEW_ASSIGNED")
                || permissionService.hasPermission(authentication, "INCIDENTS_VIEW_UNIT");
    }

    @Transactional(readOnly = true)
    public boolean canView(Authentication authentication, UUID incidentId) {
        if (authentication == null || !authentication.isAuthenticated() || incidentId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "INCIDENTS_VIEW")) {
            return incidentRepository.existsById(incidentId);
        }

        if (permissionService.hasPermission(authentication, "INCIDENTS_VIEW_OWN")
                && incidentRepository.existsByIdAndReportedByUser_EmailIgnoreCase(
                incidentId, authentication.getName())) {
            return true;
        }

        if (permissionService.hasPermission(authentication, "INCIDENTS_VIEW_ASSIGNED")
                && incidentRepository.existsByIdAndAssignedToStaff_User_EmailIgnoreCase(
                incidentId, authentication.getName())) {
            return true;
        }

        if (permissionService.hasPermission(authentication, "INCIDENTS_VIEW_UNIT")) {
            return incidentRepository.existsForOwnedUnit(incidentId, authentication.getName());
        }

        return false;
    }

    public boolean canCreate(Authentication authentication, UUID unitId) {
        if (authentication == null || !authentication.isAuthenticated() || unitId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "INCIDENTS_CREATE")) {
            return true;
        }

        if (!permissionService.hasPermission(authentication, "INCIDENTS_CREATE_OWN")) {
            return false;
        }

        return residentRepository.existsActiveResidentForUserAndUnit(
                authentication.getName(), unitId
        );
    }


    public boolean canCreateOptions(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && (permissionService.hasPermission(authentication, "INCIDENTS_CREATE")
                || permissionService.hasPermission(authentication, "INCIDENTS_CREATE_OWN"));
    }

    public boolean canUpdate(Authentication authentication, UUID incidentId) {
        if (authentication == null || !authentication.isAuthenticated() || incidentId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "INCIDENTS_UPDATE")
                && incidentRepository.existsById(incidentId)) {
            return true;
        }

        return permissionService.hasPermission(authentication, "INCIDENTS_UPDATE_OWN")
                && incidentRepository.existsByIdAndReportedByUser_EmailIgnoreCase(
                incidentId, authentication.getName());
    }

    public boolean canAssign(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && permissionService.hasPermission(authentication, "INCIDENTS_ASSIGN");
    }

    @Transactional(readOnly = true)
    public boolean canUpdateStatus(Authentication authentication, UUID incidentId) {
        if (authentication == null || !authentication.isAuthenticated() || incidentId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "INCIDENTS_UPDATE")) {
            return incidentRepository.existsById(incidentId);
        }

        return permissionService.hasPermission(authentication, "INCIDENTS_UPDATE_ASSIGNED")
                && incidentRepository.existsByIdAndAssignedToStaff_User_EmailIgnoreCase(
                incidentId,
                authentication.getName()
        );
    }
}