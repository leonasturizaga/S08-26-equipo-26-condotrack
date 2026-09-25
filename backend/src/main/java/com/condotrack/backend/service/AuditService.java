package com.condotrack.backend.service;

import com.condotrack.backend.model.AuditLog;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.AuditLogRepository;
import com.condotrack.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public void record(String actorEmail, Building building, String entityType, UUID entityId,
                       String action, String previousStatus, String newStatus, String resolution,
                       Map<String, Object> details) {
        AuditLog log = new AuditLog();
        log.setBuilding(building);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        User actor = actorEmail == null ? null : userRepository.findByEmailIgnoreCase(actorEmail).orElse(null);
        log.setActorUser(actor);
        log.setPreviousStatus(previousStatus);
        log.setNewStatus(newStatus);
        log.setResolution(resolution);
        log.setDetails(details == null ? new LinkedHashMap<>() : new LinkedHashMap<>(details));
        log.setOccurredAt(OffsetDateTime.now());
        log.setUpdatedBy(actor == null ? null : actor.getId());
        auditLogRepository.save(log);
    }

    public void record(String actorEmail, Building building, String entityType, UUID entityId,
                       String action, String previousStatus, String newStatus) {
        record(actorEmail, building, entityType, entityId, action, previousStatus, newStatus, null, Map.of());
    }

    public void record(String actorEmail, Building building, String entityType, UUID entityId,
                       String action, Map<String, Object> details) {
        record(actorEmail, building, entityType, entityId, action, null, null, null, details);
    }
}
