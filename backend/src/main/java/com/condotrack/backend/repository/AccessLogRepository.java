//--------------- milestone 14 ---------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.AccessLog;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.List;
// import java.util.UUID;

// public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {
//     List<AccessLog> findTop50ByUnitIdOrderByOccurredAtDesc(UUID unitId);
// }


//--------------- milestone 14.1 ---------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.AccessLog;
import com.condotrack.backend.model.Enums;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {
     List<AccessLog> findTop50ByUnitIdOrderByOccurredAtDesc(UUID unitId);

    @Query("""
            SELECT al
            FROM AccessLog al
            JOIN FETCH al.building
            JOIN FETCH al.unit
            LEFT JOIN FETCH al.visitor
            LEFT JOIN FETCH al.authorization
            WHERE al.direction = :direction
              AND al.authorization IS NOT NULL
              AND NOT EXISTS (
                    SELECT later.id
                    FROM AccessLog later
                    WHERE later.authorization.id = al.authorization.id
                      AND later.occurredAt > al.occurredAt
              )
            ORDER BY al.occurredAt DESC
            """)
    Page<AccessLog> findActiveVisitorEntries(
            @Param("direction") Enums.AccessDirection direction,
            Pageable pageable
    );

    @Query("""
            SELECT al
            FROM AccessLog al
            JOIN FETCH al.building
            JOIN FETCH al.unit
            LEFT JOIN FETCH al.visitor
            LEFT JOIN FETCH al.authorization
            WHERE al.direction = :direction
              AND al.authorization IS NOT NULL
              AND al.unit.id IN :unitIds
              AND NOT EXISTS (
                    SELECT later.id
                    FROM AccessLog later
                    WHERE later.authorization.id = al.authorization.id
                      AND later.occurredAt > al.occurredAt
              )
            ORDER BY al.occurredAt DESC
            """)
    Page<AccessLog> findActiveVisitorEntriesForUnits(
            @Param("direction") Enums.AccessDirection direction,
            @Param("unitIds") java.util.Collection<UUID> unitIds,
            Pageable pageable
    );

    Optional<AccessLog> findTopByAuthorization_IdOrderByOccurredAtDesc(UUID authorizationId);
}
