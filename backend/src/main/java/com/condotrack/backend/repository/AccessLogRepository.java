package com.condotrack.backend.repository;

import com.condotrack.backend.model.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {
    List<AccessLog> findTop50ByUnitIdOrderByOccurredAtDesc(UUID unitId);
}
