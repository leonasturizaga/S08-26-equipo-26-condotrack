package com.condotrack.backend.service;

import com.condotrack.backend.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("accessAccessService")
@RequiredArgsConstructor
public class AccessAccessService {

    private final PermissionService permissionService;
    private final ResidentRepository residentRepository;

    @Transactional(readOnly = true)
    public boolean canCreateVisitorAuthorization(
            Authentication authentication,
            UUID unitId,
            UUID residentId
    ) {
        if (authentication == null || !authentication.isAuthenticated() || unitId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "ACCESS_CREATE")) {
            return true;
        }

        if (!permissionService.hasPermission(authentication, "ACCESS_CREATE_OWN")) {
            return false;
        }

        boolean ownUnit = residentRepository.existsActiveResidentForUserAndUnit(
                authentication.getName(),
                unitId
        );

        if (!ownUnit) {
            return false;
        }

        if (residentId == null) {
            return true;
        }

        return residentRepository.existsActiveResidentForUserAndResidentId(
                authentication.getName(),
                residentId
        );
    }
}
