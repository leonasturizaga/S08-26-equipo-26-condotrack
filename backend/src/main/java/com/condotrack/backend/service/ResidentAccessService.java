
//-------------- milestone 6 ---------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.repository.ResidentRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.UUID;

// @Service("residentAccessService")
// @RequiredArgsConstructor
// public class ResidentAccessService {

//     private final PermissionService permissionService;
//     private final ResidentRepository residentRepository;

//     @Transactional(readOnly = true)
//     public boolean canView(Authentication authentication, UUID residentId) {
//         if (authentication == null || !authentication.isAuthenticated() || residentId == null) {
//             return false;
//         }

//         if (permissionService.hasPermission(authentication, "RESIDENTS_VIEW")) {
//             return true;
//         }

//         if (!permissionService.hasPermission(authentication, "RESIDENTS_VIEW_OWN")) {
//             return false;
//         }

//         return residentRepository.existsActiveResidentForUserAndResidentId(
//                 authentication.getName(),
//                 residentId
//         );
//     }

//     @Transactional(readOnly = true)
//     public boolean canUpdate(Authentication authentication, UUID residentId) {
//         if (authentication == null || !authentication.isAuthenticated() || residentId == null) {
//             return false;
//         }

//         if (permissionService.hasPermission(authentication, "RESIDENTS_UPDATE")) {
//             return true;
//         }

//         if (!permissionService.hasPermission(authentication, "RESIDENTS_UPDATE_OWN")) {
//             return false;
//         }

//         return residentRepository.existsActiveResidentForUserAndResidentId(
//                 authentication.getName(),
//                 residentId
//         );
//     }
// }


//---------------- milestone 7 ----------------------
package com.condotrack.backend.service;

import com.condotrack.backend.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("residentAccessService")
@RequiredArgsConstructor
public class ResidentAccessService {

    private final PermissionService permissionService;
    private final ResidentRepository residentRepository;

    @Transactional(readOnly = true)
    public boolean canList(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return permissionService.hasPermission(authentication, "RESIDENTS_VIEW")
                || permissionService.hasPermission(authentication, "RESIDENTS_VIEW_OWN");
    }

    @Transactional(readOnly = true)
    public boolean canView(Authentication authentication, UUID residentId) {
        if (authentication == null || !authentication.isAuthenticated() || residentId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "RESIDENTS_VIEW")) {
            return true;
        }

        if (!permissionService.hasPermission(authentication, "RESIDENTS_VIEW_OWN")) {
            return false;
        }

        return residentRepository.existsActiveResidentForUserAndResidentId(
                authentication.getName(),
                residentId
        );
    }

    @Transactional(readOnly = true)
    public boolean canUpdate(Authentication authentication, UUID residentId) {
        if (authentication == null || !authentication.isAuthenticated() || residentId == null) {
            return false;
        }

        if (permissionService.hasPermission(authentication, "RESIDENTS_UPDATE")) {
            return true;
        }

        if (!permissionService.hasPermission(authentication, "RESIDENTS_UPDATE_OWN")) {
            return false;
        }

        return residentRepository.existsActiveResidentForUserAndResidentId(
                authentication.getName(),
                residentId
        );
    }
}