package com.condotrack.backend.rbac;

import com.condotrack.backend.model.AuditLog;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.BuildingRolePermission;
import com.condotrack.backend.model.Permission;
import com.condotrack.backend.model.Role;
import com.condotrack.backend.model.RolePermission;
import com.condotrack.backend.repository.AuditLogRepository;
import com.condotrack.backend.repository.BuildingRepository;
import com.condotrack.backend.repository.BuildingRolePermissionRepository;
import com.condotrack.backend.repository.PermissionRepository;
import com.condotrack.backend.repository.RolePermissionRepository;
import com.condotrack.backend.repository.RoleRepository;
import com.condotrack.backend.repository.UserRepository;
import com.condotrack.backend.rbac.dto.RbacAssignmentResponse;
import com.condotrack.backend.rbac.dto.RbacAuditPageResponse;
import com.condotrack.backend.rbac.dto.RbacAuditResponse;
import com.condotrack.backend.rbac.dto.RbacMatrixResponse;
import com.condotrack.backend.rbac.dto.RbacPermissionResponse;
import com.condotrack.backend.rbac.dto.RbacPermissionUpdateRequest;
import com.condotrack.backend.rbac.dto.RbacRoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RbacService {

    private static final String AUDIT_ENTITY_TYPE = "RBAC_PERMISSION";
    private static final String RBAC_UPDATE_PERMISSION = "RBAC_MANAGEMENT_UPDATE";
    private static final String RBAC_VIEW_PERMISSION = "RBAC_MANAGEMENT_VIEW";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final BuildingRolePermissionRepository buildingRolePermissionRepository;
    private final BuildingRepository buildingRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public RbacMatrixResponse getMatrix(UUID buildingId) {
        if (buildingId != null) {
            buildingRepository.findById(buildingId)
                    .orElseThrow(() -> new IllegalArgumentException("Building not found"));
        }

        List<Role> roles = roleRepository.findAll().stream()
                .sorted(Comparator.comparing(Role::getCode))
                .toList();
        List<Permission> permissions = permissionRepository.findAll().stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .toList();

        Map<String, Map<String, Boolean>> global = new HashMap<>();
        for (RolePermission rp : rolePermissionRepository.findAll()) {
            global.computeIfAbsent(rp.getRole().getCode(), ignored -> new HashMap<>())
                    .put(rp.getPermission().getCode(), rp.isActive());
        }

        Map<String, Map<String, Boolean>> overrides = new HashMap<>();
        if (buildingId != null) {
            for (BuildingRolePermission brp : buildingRolePermissionRepository.findAllForBuilding(buildingId)) {
                overrides.computeIfAbsent(brp.getRole().getCode(), ignored -> new HashMap<>())
                        .put(brp.getPermission().getCode(), brp.isActive());
            }
        }

        List<RbacAssignmentResponse> assignments = new ArrayList<>();
        for (Role role : roles) {
            for (Permission permission : permissions) {
                String roleCode = role.getCode();
                String permissionCode = permission.getCode();
                boolean overridden = buildingId != null
                        && overrides.containsKey(roleCode)
                        && overrides.get(roleCode).containsKey(permissionCode);
                boolean active = overridden
                        ? overrides.get(roleCode).get(permissionCode)
                        : global.getOrDefault(roleCode, Map.of()).getOrDefault(permissionCode, false);

                assignments.add(new RbacAssignmentResponse(
                        roleCode,
                        permissionCode,
                        active,
                        overridden,
                        overridden ? "BUILDING_OVERRIDE" : "GLOBAL"
                ));
            }
        }

        List<RbacRoleResponse> roleResponses = roles.stream()
                .map(role -> new RbacRoleResponse(role.getId(), role.getCode(), role.getName()))
                .toList();

        List<RbacPermissionResponse> permissionResponses = permissions.stream()
                .map(this::toPermissionResponse)
                .toList();

        return new RbacMatrixResponse(
                buildingId,
                buildingId == null ? "GLOBAL" : "BUILDING",
                roleResponses,
                permissionResponses,
                assignments
        );
    }

    @Transactional
    public RbacAssignmentResponse updatePermission(
            RbacPermissionUpdateRequest request,
            String actorEmail
    ) {
        Role role = roleRepository.findByCode(request.roleCode().trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        Permission permission = permissionRepository.findByCode(request.permissionCode().trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));

        if (request.buildingId() != null && "ADMINISTRATOR".equals(role.getCode())) {
            throw new IllegalStateException("Administrator permissions are managed globally and cannot be overridden per building.");
        }

        if (request.buildingId() == null
                && "ADMINISTRATOR".equals(role.getCode())
                && (RBAC_UPDATE_PERMISSION.equals(permission.getCode()) || RBAC_VIEW_PERMISSION.equals(permission.getCode()))
                && !request.active()) {
            throw new IllegalStateException("RBAC management permissions cannot be globally disabled for Administrator.");
        }

        if (request.buildingId() == null) {
            com.condotrack.backend.model.RolePermissionId id =
                    new com.condotrack.backend.model.RolePermissionId(role.getId(), permission.getId());
            RolePermission rolePermission = rolePermissionRepository.findById(id).orElse(null);
            boolean previous = rolePermission != null && rolePermission.isActive();
            if (rolePermission == null) {
                rolePermission = new RolePermission(role, permission, request.active());
            } else {
                rolePermission.setActive(request.active());
            }
            rolePermission.setUpdatedBy(findActorId(actorEmail));
            RolePermission saved = rolePermissionRepository.save(rolePermission);

            if (previous != request.active()) {
                writeAudit(actorEmail, null, role, permission, previous, request.active(), "GLOBAL");
            }

            return new RbacAssignmentResponse(role.getCode(), permission.getCode(), saved.isActive(), false, "GLOBAL");
        }

        Building building = buildingRepository.findById(request.buildingId())
                .orElseThrow(() -> new IllegalArgumentException("Building not found"));

        var id = new com.condotrack.backend.model.BuildingRolePermissionId(
                building.getId(), role.getId(), permission.getId());
        BuildingRolePermission existing = buildingRolePermissionRepository.findById(id).orElse(null);
        boolean previousEffective = existing != null
                ? existing.isActive()
                : effectiveGlobal(role.getCode(), permission.getCode());

        BuildingRolePermission override = existing;
        if (override == null) {
            override = new BuildingRolePermission(building, role, permission, request.active());
        } else {
            override.setActive(request.active());
        }
        override.setUpdatedBy(findActorId(actorEmail));
        BuildingRolePermission saved = buildingRolePermissionRepository.save(override);

        if (previousEffective != request.active()) {
            writeAudit(actorEmail, building, role, permission, previousEffective, request.active(), "BUILDING_OVERRIDE");
        }

        return new RbacAssignmentResponse(role.getCode(), permission.getCode(), saved.isActive(), true, "BUILDING_OVERRIDE");
    }

    @Transactional
    public void resetBuildingOverride(UUID buildingId, String roleCode, String permissionCode, String actorEmail) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("Building not found"));
        Role role = roleRepository.findByCode(roleCode.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        Permission permission = permissionRepository.findByCode(permissionCode.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));

        var id = new com.condotrack.backend.model.BuildingRolePermissionId(
                building.getId(), role.getId(), permission.getId());
        BuildingRolePermission existing = buildingRolePermissionRepository.findById(id).orElse(null);
        if (existing == null) {
            return;
        }

        boolean previous = existing.isActive();
        boolean next = effectiveGlobal(role.getCode(), permission.getCode());
        buildingRolePermissionRepository.delete(existing);

        if (previous != next) {
            writeAudit(actorEmail, building, role, permission, previous, next, "BUILDING_RESET_TO_GLOBAL");
        }
    }

    @Transactional(readOnly = true)
    public RbacAuditPageResponse getAudit(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<AuditLog> result = auditLogRepository.findByEntityTypeOrderByOccurredAtDesc(AUDIT_ENTITY_TYPE, pageable);

        List<RbacAuditResponse> items = result.getContent().stream()
                .map(this::toAuditResponse)
                .toList();

        return new RbacAuditPageResponse(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private boolean effectiveGlobal(String roleCode, String permissionCode) {
        return rolePermissionRepository.existsActiveForRoleAndPermission(roleCode, permissionCode);
    }

    private UUID findActorId(String actorEmail) {
        if (actorEmail == null || actorEmail.isBlank()) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(actorEmail).map(com.condotrack.backend.model.User::getId).orElse(null);
    }

    private void writeAudit(
            String actorEmail,
            Building building,
            Role role,
            Permission permission,
            boolean previous,
            boolean next,
            String scope
    ) {
        AuditLog log = new AuditLog();
        log.setBuilding(building);
        log.setEntityType(AUDIT_ENTITY_TYPE);
        log.setEntityId(permission.getId());
        log.setAction("PERMISSION_CHANGED");
        log.setActorUser(userRepository.findByEmailIgnoreCase(actorEmail).orElse(null));
        log.setPreviousStatus(Boolean.toString(previous));
        log.setNewStatus(Boolean.toString(next));
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("roleCode", role.getCode());
        details.put("permissionCode", permission.getCode());
        details.put("previousActive", previous);
        details.put("newActive", next);
        details.put("scope", scope);
        if (building != null) {
            details.put("buildingId", building.getId().toString());
            details.put("buildingCode", building.getCode());
        }
        log.setDetails(details);
        log.setOccurredAt(OffsetDateTime.now());
        log.setUpdatedBy(findActorId(actorEmail));
        auditLogRepository.save(log);
    }

    private RbacPermissionResponse toPermissionResponse(Permission permission) {
        String code = permission.getCode();
        String base = code;
        String scope = "ALL";
        for (String candidate : List.of("OWN", "UNIT", "ASSIGNED")) {
            String suffix = "_" + candidate;
            if (base.endsWith(suffix)) {
                scope = candidate;
                base = base.substring(0, base.length() - suffix.length());
                break;
            }
        }

        int separator = base.lastIndexOf('_');
        String module = separator > 0 ? base.substring(0, separator) : base;
        String action = separator > 0 ? base.substring(separator + 1) : "OTHER";

        return new RbacPermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getDescription(),
                module,
                action,
                scope
        );
    }

    private RbacAuditResponse toAuditResponse(AuditLog log) {
        Map<String, Object> details = log.getDetails() == null ? Map.of() : log.getDetails();
        Boolean previous = asBoolean(details.get("previousActive"));
        Boolean next = asBoolean(details.get("newActive"));
        return new RbacAuditResponse(
                log.getId(),
                log.getOccurredAt(),
                log.getBuilding() == null ? null : log.getBuilding().getId(),
                log.getBuilding() == null ? null : log.getBuilding().getCode(),
                log.getAction(),
                stringValue(details.get("roleCode")),
                stringValue(details.get("permissionCode")),
                previous,
                next,
                stringValue(details.get("scope")),
                log.getActorUser() == null ? null : log.getActorUser().getEmail()
        );
    }

    private Boolean asBoolean(Object value) {
        if (value instanceof Boolean booleanValue) return booleanValue;
        if (value == null) return null;
        return Boolean.valueOf(value.toString());
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

}
