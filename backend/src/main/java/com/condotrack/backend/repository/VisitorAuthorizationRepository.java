//-------------- milestone 14.1 ---------------------
// package com.condotrack.backend.repository;

// import com.condotrack.backend.model.Enums;
// import com.condotrack.backend.model.VisitorAuthorization;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import java.time.OffsetDateTime;
// import java.util.Collection;
// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;

// public interface VisitorAuthorizationRepository extends JpaRepository<VisitorAuthorization, UUID> {

//     @Query("""
//             SELECT va
//             FROM VisitorAuthorization va
//             JOIN FETCH va.building
//             JOIN FETCH va.unit
//             JOIN FETCH va.visitor
//             LEFT JOIN FETCH va.resident
//             WHERE va.id = :id
//             """)
//     Optional<VisitorAuthorization> findForAccessOperation(@Param("id") UUID id);

//     @Query("""
//             SELECT va
//             FROM VisitorAuthorization va
//             JOIN FETCH va.building
//             JOIN FETCH va.unit
//             JOIN FETCH va.visitor
//             LEFT JOIN FETCH va.resident
//             WHERE va.qrToken = :qrToken
//             """)
//     Optional<VisitorAuthorization> findForAccessOperationByQrToken(@Param("qrToken") String qrToken);

//     Page<VisitorAuthorization> findAllByOrderByCreatedAtDesc(Pageable pageable);

//     Page<VisitorAuthorization> findByStatusOrderByCreatedAtDesc(
//             Enums.VisitorAuthorizationStatus status,
//             Pageable pageable
//     );

//     Page<VisitorAuthorization> findByUnitIdInOrderByCreatedAtDesc(
//             Collection<UUID> unitIds,
//             Pageable pageable
//     );

//     Page<VisitorAuthorization> findByUnitIdInAndStatusOrderByCreatedAtDesc(
//             Collection<UUID> unitIds,
//             Enums.VisitorAuthorizationStatus status,
//             Pageable pageable
//     );

//     @Query("""
//             SELECT va
//             FROM VisitorAuthorization va
//             WHERE va.status = :status
//               AND va.validUntil <= :now
//             ORDER BY va.createdAt DESC
//             """)
//     Page<VisitorAuthorization> findApprovedExpired(
//             @Param("status") Enums.VisitorAuthorizationStatus status,
//             @Param("now") OffsetDateTime now,
//             Pageable pageable
//     );

//     @Query("""
//             SELECT va
//             FROM VisitorAuthorization va
//             WHERE va.unit.id IN :unitIds
//               AND va.status = :status
//               AND va.validUntil <= :now
//             ORDER BY va.createdAt DESC
//             """)
//     Page<VisitorAuthorization> findApprovedExpiredForUnits(
//             @Param("unitIds") Collection<UUID> unitIds,
//             @Param("status") Enums.VisitorAuthorizationStatus status,
//             @Param("now") OffsetDateTime now,
//             Pageable pageable
//     );

//     boolean existsByQrToken(String qrToken);

//     boolean existsByIdAndUnitIdIn(UUID id, Collection<UUID> unitIds);
// }


//-------------------- M23.1 -----------------------------
package com.condotrack.backend.repository;

import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.VisitorAuthorization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    @Query("""
            SELECT va
            FROM VisitorAuthorization va
            JOIN FETCH va.building
            JOIN FETCH va.unit
            JOIN FETCH va.visitor
            LEFT JOIN FETCH va.resident
            ORDER BY va.validFrom DESC
            """)
    Page<VisitorAuthorization> findAllForView(Pageable pageable);

    @Query("""
            SELECT va
            FROM VisitorAuthorization va
            JOIN FETCH va.building
            JOIN FETCH va.unit
            JOIN FETCH va.visitor
            LEFT JOIN FETCH va.resident
            WHERE va.status = :status
            ORDER BY va.validFrom DESC
            """)
    Page<VisitorAuthorization> findAllForViewByStatus(@Param("status") Enums.VisitorAuthorizationStatus status, Pageable pageable);

    @Query("""
            SELECT va
            FROM VisitorAuthorization va
            JOIN FETCH va.building
            JOIN FETCH va.unit
            JOIN FETCH va.visitor
            LEFT JOIN FETCH va.resident
            WHERE LOWER(va.resident.user.email) = LOWER(:email)
            ORDER BY va.validFrom DESC
            """)
    Page<VisitorAuthorization> findForUserView(@Param("email") String email, Pageable pageable);

    @Query("""
            SELECT va
            FROM VisitorAuthorization va
            JOIN FETCH va.building
            JOIN FETCH va.unit
            JOIN FETCH va.visitor
            LEFT JOIN FETCH va.resident
            WHERE LOWER(va.resident.user.email) = LOWER(:email)
              AND va.status = :status
            ORDER BY va.validFrom DESC
            """)
    Page<VisitorAuthorization> findForUserViewByStatus(
            @Param("email") String email,
            @Param("status") Enums.VisitorAuthorizationStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT va
            FROM VisitorAuthorization va
            JOIN FETCH va.building
            JOIN FETCH va.unit
            JOIN FETCH va.visitor
            LEFT JOIN FETCH va.resident
            WHERE va.id = :id
            """)
    Optional<VisitorAuthorization> findForView(@Param("id") UUID id);

    @Query("""
            SELECT va
            FROM VisitorAuthorization va
            JOIN FETCH va.building
            JOIN FETCH va.unit
            JOIN FETCH va.visitor
            LEFT JOIN FETCH va.resident
            WHERE va.id = :id
              AND LOWER(va.resident.user.email) = LOWER(:email)
            """)
    Optional<VisitorAuthorization> findForUserViewById(@Param("id") UUID id, @Param("email") String email);

    boolean existsByQrToken(String qrToken);

    boolean existsByIdAndUnitIdIn(UUID id, Collection<UUID> unitIds);
}
