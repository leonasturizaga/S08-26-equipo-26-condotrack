package com.condotrack.backend.service;

import com.condotrack.backend.dto.AuditLogPageResponse;
import com.condotrack.backend.model.AuditLog;
import com.condotrack.backend.repository.AuditLogRepository;
import com.condotrack.backend.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditQueryService {
    private final AuditLogRepository auditLogRepository;
    private final BuildingRepository buildingRepository;

    @Transactional(readOnly = true)
    public AuditLogPageResponse search(int page, int size, String entityType, String action,
                                      UUID buildingId, String actorEmail, OffsetDateTime from, OffsetDateTime to) {
        if (buildingId != null) {
            buildingRepository.findById(buildingId).orElseThrow(() -> new IllegalArgumentException("Building not found: " + buildingId));
        }
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "occurredAt"));
        Specification<AuditLog> spec = Specification.where(null);
        if (entityType != null && !entityType.isBlank()) {
            String value = entityType.trim().toLowerCase();
            spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("entityType")), value));
        }
        if (action != null && !action.isBlank()) {
            String value = action.trim().toLowerCase();
            spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("action")), value));
        }
        if (buildingId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("building").get("id"), buildingId));
        }
        if (actorEmail != null && !actorEmail.isBlank()) {
            String value = actorEmail.trim().toLowerCase();
            spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("actorUser").get("email")), value));
        }
        if (from != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), to));
        }
        Page<AuditLog> result = auditLogRepository.findAll(spec, pageable);
        return AuditLogPageResponse.from(result.map(this::toResponse));
    }

    private com.condotrack.backend.dto.AuditLogResponse toResponse(AuditLog log) {
        return new com.condotrack.backend.dto.AuditLogResponse(
                log.getId(), log.getOccurredAt(),
                log.getBuilding() == null ? null : log.getBuilding().getId(),
                log.getBuilding() == null ? null : log.getBuilding().getCode(),
                log.getEntityType(), log.getEntityId(), log.getAction(),
                log.getActorUser() == null ? null : log.getActorUser().getId().toString(),
                log.getActorUser() == null ? null : log.getActorUser().getEmail(),
                log.getPreviousStatus(), log.getNewStatus(), log.getResolution(),
                log.getDetails() == null ? Map.of() : log.getDetails()
        );
    }
}
