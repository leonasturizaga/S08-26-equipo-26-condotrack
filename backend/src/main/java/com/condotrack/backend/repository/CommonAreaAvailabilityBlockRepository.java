package com.condotrack.backend.repository;

import com.condotrack.backend.model.CommonAreaAvailabilityBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface CommonAreaAvailabilityBlockRepository
        extends JpaRepository<CommonAreaAvailabilityBlock, UUID> {

    List<CommonAreaAvailabilityBlock>
    findByCommonAreaIdAndActiveTrueOrderByStartAtAsc(
            UUID commonAreaId
    );

    List<CommonAreaAvailabilityBlock>
    findByCommonAreaIdAndActiveTrueAndEndAtGreaterThanAndStartAtLessThanOrderByStartAtAsc(
            UUID commonAreaId,
            OffsetDateTime from,
            OffsetDateTime to
    );

    boolean existsByCommonAreaIdAndActiveTrueAndStartAtLessThanAndEndAtGreaterThan(
            UUID commonAreaId,
            OffsetDateTime endAt,
            OffsetDateTime startAt
    );

    boolean existsByCommonAreaIdAndActiveTrueAndIdNotAndStartAtLessThanAndEndAtGreaterThan(
            UUID commonAreaId,
            UUID id,
            OffsetDateTime endAt,
            OffsetDateTime startAt
    );
}