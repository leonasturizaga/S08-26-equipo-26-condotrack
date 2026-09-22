package com.condotrack.backend.controller;

import com.condotrack.backend.rbac.RbacService;
import com.condotrack.backend.rbac.dto.RbacAuditPageResponse;
import com.condotrack.backend.rbac.dto.RbacMatrixResponse;
import com.condotrack.backend.rbac.dto.RbacPermissionUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
@Tag(name = "RBAC Administration", description = "Administrator control of role and permission assignments.")
public class RbacController {

    private final RbacService rbacService;

    @GetMapping("/matrix")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'RBAC_MANAGEMENT_VIEW')")
    @Operation(summary = "Get the RBAC matrix", description = "Returns global defaults or effective permissions for a selected building.")
    public RbacMatrixResponse getMatrix(@RequestParam(required = false) UUID buildingId) {
        return rbacService.getMatrix(buildingId);
    }

    @PutMapping("/permission-assignments")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'RBAC_MANAGEMENT_UPDATE')")
    @Operation(summary = "Activate or deactivate a permission assignment")
    public RbacMatrixResponse updatePermission(
            @Valid @RequestBody RbacPermissionUpdateRequest request,
            Authentication authentication
    ) {
        rbacService.updatePermission(request, authentication.getName());
        return rbacService.getMatrix(request.buildingId());
    }

    @DeleteMapping("/buildings/{buildingId}/permission-overrides/{roleCode}/{permissionCode}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'RBAC_MANAGEMENT_UPDATE')")
    @Operation(summary = "Reset a building permission override to the global default")
    public ResponseEntity<RbacMatrixResponse> resetBuildingOverride(
            @PathVariable UUID buildingId,
            @PathVariable String roleCode,
            @PathVariable String permissionCode,
            Authentication authentication
    ) {
        rbacService.resetBuildingOverride(buildingId, roleCode, permissionCode, authentication.getName());
        return ResponseEntity.ok(rbacService.getMatrix(buildingId));
    }

    @GetMapping("/audit")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'RBAC_MANAGEMENT_VIEW')")
    @Operation(summary = "Get RBAC permission change audit entries")
    public RbacAuditPageResponse getAudit(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return rbacService.getAudit(page, size);
    }
}
