//------------------------- milestone 4a ----------------------
// package com.condotrack.backend.service;

// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;

// @Service("permissionService")
// public class PermissionService {

//     public boolean hasPermission(
//             Authentication authentication,
//             String permissionCode
//     ) {
//         if (authentication == null || !authentication.isAuthenticated()) {
//             return false;
//         }

//         String requiredAuthority = "PERM_" + permissionCode;

//         return authentication.getAuthorities()
//                 .stream()
//                 .anyMatch(authority ->
//                         authority.getAuthority()
//                                 .equals(requiredAuthority)
//                 );
//     }
// }


//------------------------- milestone 17.1 ----------------------
package com.condotrack.backend.service;

import com.condotrack.backend.model.BuildingRolePermission;
import com.condotrack.backend.model.RolePermission;
import com.condotrack.backend.repository.BuildingRolePermissionRepository;
import com.condotrack.backend.repository.RolePermissionRepository;
import com.condotrack.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service("permissionService")
@RequiredArgsConstructor
public class PermissionService {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final BuildingRolePermissionRepository buildingRolePermissionRepository;

    @Transactional(readOnly = true)
    public boolean hasPermission(Authentication authentication, String permissionCode) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Set<String> roleCodes = userRepository.findRoleCodesByEmailIgnoreCase(authentication.getName());
        if (roleCodes.isEmpty()) {
            return false;
        }

        return rolePermissionRepository.findByRoleCodes(roleCodes).stream()
                .anyMatch(rp -> permissionCode.equals(rp.getPermission().getCode()) && rp.isActive());
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(Authentication authentication, String permissionCode, UUID buildingId) {
        if (authentication == null || !authentication.isAuthenticated() || buildingId == null) {
            return hasPermission(authentication, permissionCode);
        }

        Set<String> roleCodes = userRepository.findRoleCodesByEmailIgnoreCase(authentication.getName());
        if (roleCodes.isEmpty()) {
            return false;
        }

        // Administrators have global, building-independent permissions.
        // Building-specific overrides are not applicable to this role.
        if (roleCodes.contains("ADMINISTRATOR")) {
            return rolePermissionRepository.findByRoleCodes(Set.of("ADMINISTRATOR")).stream()
                    .anyMatch(rp -> permissionCode.equals(rp.getPermission().getCode()) && rp.isActive());
        }
        Map<String, Boolean> global = new HashMap<>();
        for (RolePermission rp : rolePermissionRepository.findByRoleCodes(roleCodes)) {
            if (permissionCode.equals(rp.getPermission().getCode())) {
                global.put(rp.getRole().getCode(), rp.isActive());
            }
        }

        Map<String, Boolean> overrides = new HashMap<>();
        for (BuildingRolePermission brp : buildingRolePermissionRepository.findByBuildingIdAndRoleCodes(buildingId, roleCodes)) {
            if (permissionCode.equals(brp.getPermission().getCode())) {
                overrides.put(brp.getRole().getCode(), brp.isActive());
            }
        }

        for (String roleCode : roleCodes) {
            boolean active = overrides.containsKey(roleCode)
                    ? overrides.get(roleCode)
                    : global.getOrDefault(roleCode, false);
            if (active) {
                return true;
            }
        }

        return false;
    }
}
