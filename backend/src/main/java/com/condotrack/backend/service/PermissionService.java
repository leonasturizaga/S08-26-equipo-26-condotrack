//-------------- milestone 4 ---------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.repository.RoleRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.Set;
// import java.util.stream.Collectors;

// @Service("permissionService")
// @RequiredArgsConstructor
// public class PermissionService {

//     private final RoleRepository roleRepository;

//     @Transactional(readOnly = true)
//     public boolean hasPermission(Authentication authentication, String permissionCode) {
//         if (authentication == null || !authentication.isAuthenticated()) {
//             return false;
//         }

//         Set<String> roleCodes = authentication.getAuthorities().stream()
//                 .map(authority -> authority.getAuthority())
//                 .filter(authority -> authority.startsWith("ROLE_"))
//                 .map(authority -> authority.substring("ROLE_".length()))
//                 .collect(Collectors.toSet());

//         if (roleCodes.isEmpty()) {
//             return false;
//         }

//         return roleRepository.existsPermissionForRoles(roleCodes, permissionCode);
//     }
// }


//------------------------- milestone 4a ----------------------
package com.condotrack.backend.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("permissionService")
public class PermissionService {

    public boolean hasPermission(
            Authentication authentication,
            String permissionCode
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String requiredAuthority = "PERM_" + permissionCode;

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals(requiredAuthority)
                );
    }
}