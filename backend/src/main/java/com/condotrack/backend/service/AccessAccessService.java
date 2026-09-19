//--------------- milestone 14 ---------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.repository.ResidentRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.UUID;

// @Service("accessAccessService")
// @RequiredArgsConstructor
// public class AccessAccessService {

//     private final PermissionService permissionService;
//     private final ResidentRepository residentRepository;

//     @Transactional(readOnly = true)
//     public boolean canCreateVisitorAuthorization(
//             Authentication authentication,
//             UUID unitId,
//             UUID residentId
//     ) {
//         if (authentication == null || !authentication.isAuthenticated() || unitId == null) {
//             return false;
//         }

//         if (permissionService.hasPermission(authentication, "ACCESS_CREATE")) {
//             return true;
//         }

//         if (!permissionService.hasPermission(authentication, "ACCESS_CREATE_OWN")) {
//             return false;
//         }

//         boolean ownUnit = residentRepository.existsActiveResidentForUserAndUnit(
//                 authentication.getName(),
//                 unitId
//         );

//         if (!ownUnit) {
//             return false;
//         }

//         if (residentId == null) {
//             return true;
//         }

//         return residentRepository.existsActiveResidentForUserAndResidentId(
//                 authentication.getName(),
//                 residentId
//         );
//     }
// }


//------------------- milestone 14.1 ---------------------
package com.condotrack.backend.service;

import com.condotrack.backend.repository.ResidentRepository;
import com.condotrack.backend.repository.VisitorAuthorizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service("accessAccessService")
@RequiredArgsConstructor
public class AccessAccessService {

    private final PermissionService permissionService;
    private final ResidentRepository residentRepository;
    private final VisitorAuthorizationRepository visitorAuthorizationRepository;

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

    @Transactional(readOnly = true)
    public boolean canViewAuthorization(
            Authentication authentication,
            UUID authorizationId
    ) {
        if (authentication == null || !authentication.isAuthenticated() || authorizationId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "ACCESS_VIEW")) {
            return visitorAuthorizationRepository.existsById(authorizationId);
        }

        if (!permissionService.hasPermission(authentication, "ACCESS_VIEW_OWN")) {
            return false;
        }

        List<UUID> ownUnitIds = getOwnActiveUnitIds(authentication);
        return !ownUnitIds.isEmpty()
                && visitorAuthorizationRepository.existsByIdAndUnitIdIn(authorizationId, ownUnitIds);
    }

    public boolean canOperateStaff(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && permissionService.hasPermission(authentication, "ACCESS_UPDATE");
    }

    private List<UUID> getOwnActiveUnitIds(Authentication authentication) {
        return residentRepository.findAllByUser_EmailIgnoreCaseAndActiveTrue(authentication.getName())
                .stream()
                .map(resident -> resident.getUnit().getId())
                .distinct()
                .toList();
    }
}