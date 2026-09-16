package com.condotrack.backend.repository;

import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.VisitorAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VisitorAuthorizationRepository extends JpaRepository<VisitorAuthorization, UUID> {

    @Query("""
            SELECT va
            FROM VisitorAuthorization va
            JOIN FETCH va.building
            JOIN FETCH va.unit
            JOIN FETCH va.visitor
            LEFT JOIN FETCH va.resident
            WHERE va.id = :id
            """)
    Optional<VisitorAuthorization> findForAccessOperation(@Param("id") UUID id);

    boolean existsByQrToken(String qrToken);
}
