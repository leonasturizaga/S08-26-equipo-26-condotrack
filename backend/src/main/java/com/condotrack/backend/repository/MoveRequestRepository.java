package com.condotrack.backend.repository;

import com.condotrack.backend.model.Enums.MoveRequestStatus;
import com.condotrack.backend.model.MoveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MoveRequestRepository extends JpaRepository<MoveRequest, UUID> {
    List<MoveRequest> findByUnitIdAndStatusNotInOrderByRequestedAtDesc(UUID unitId, List<MoveRequestStatus> excludedStatuses);
}
