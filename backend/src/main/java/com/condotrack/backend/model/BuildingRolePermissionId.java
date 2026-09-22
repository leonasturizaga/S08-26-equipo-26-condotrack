package com.condotrack.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class BuildingRolePermissionId implements Serializable {
    @Column(name = "building_id", nullable = false)
    private UUID buildingId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    public BuildingRolePermissionId(UUID buildingId, UUID roleId, UUID permissionId) {
        this.buildingId = buildingId;
        this.roleId = roleId;
        this.permissionId = permissionId;
    }
}
