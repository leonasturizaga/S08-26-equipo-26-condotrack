package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = @Index(name = "idx_audit_logs_entity", columnList = "entity_type, entity_id, occurred_at"))
@Getter @Setter @NoArgsConstructor
public class AuditLog extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "building_id") private Building building;
    @Column(name = "entity_type", nullable = false, length = 80) private String entityType;
    @Column(name = "entity_id", nullable = false) private UUID entityId;
    @Column(nullable = false, length = 80) private String action;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "actor_user_id") private User actorUser;
    @Column(name = "previous_status", length = 50) private String previousStatus;
    @Column(name = "new_status", length = 50) private String newStatus;
    @Column(columnDefinition = "TEXT") private String resolution;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb") private Map<String, Object> details;
    @Column(name = "occurred_at", nullable = false) private OffsetDateTime occurredAt;
}
