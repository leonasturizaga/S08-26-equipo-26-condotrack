//--------------------------- M23.2 -------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {

    List<AccessLog> findTop50ByUnitIdOrderByOccurredAtDesc(UUID unitId);

    @Query("""
            SELECT al
            FROM AccessLog al
            JOIN FETCH al.building
            JOIN FETCH al.unit
            LEFT JOIN FETCH al.visitor
            LEFT JOIN FETCH al.authorization
            WHERE al.direction = com.condotrack.backend.model.Enums.AccessDirection.IN
              AND NOT EXISTS (
                  SELECT outLog.id
                  FROM AccessLog outLog
                  WHERE outLog.direction = com.condotrack.backend.model.Enums.AccessDirection.OUT
                    AND outLog.authorization = al.authorization
                    AND outLog.occurredAt > al.occurredAt
              )
            ORDER BY al.occurredAt DESC
            """)
    Page<AccessLog> findActiveVisitors(Pageable pageable);

    @Query("""
            SELECT al
            FROM AccessLog al
            JOIN FETCH al.building
            JOIN FETCH al.unit
            LEFT JOIN FETCH al.visitor
            LEFT JOIN FETCH al.authorization
            JOIN al.authorization va
            JOIN va.resident resident
            WHERE al.direction = com.condotrack.backend.model.Enums.AccessDirection.IN
              AND LOWER(resident.user.email) = LOWER(:email)
              AND NOT EXISTS (
                  SELECT outLog.id
                  FROM AccessLog outLog
                  WHERE outLog.direction = com.condotrack.backend.model.Enums.AccessDirection.OUT
                    AND outLog.authorization = al.authorization
                    AND outLog.occurredAt > al.occurredAt
              )
            ORDER BY al.occurredAt DESC
            """)
    Page<AccessLog> findActiveVisitorsForUser(
            @Param("email") String email,
            Pageable pageable
    );

    @Query("""
        SELECT al
        FROM AccessLog al
        JOIN FETCH al.building
        JOIN FETCH al.unit
        LEFT JOIN FETCH al.visitor
        LEFT JOIN FETCH al.authorization
        WHERE al.authorization.id = :authorizationId
          AND al.direction = com.condotrack.backend.model.Enums.AccessDirection.IN
          AND NOT EXISTS (
              SELECT outLog.id
              FROM AccessLog outLog
              WHERE outLog.direction = com.condotrack.backend.model.Enums.AccessDirection.OUT
                AND outLog.authorization = al.authorization
                AND outLog.occurredAt > al.occurredAt
          )
        ORDER BY al.occurredAt DESC
        """)
   Optional<AccessLog> findActiveEntryByAuthorizationId(
         @Param("authorizationId") UUID authorizationId
   );

}