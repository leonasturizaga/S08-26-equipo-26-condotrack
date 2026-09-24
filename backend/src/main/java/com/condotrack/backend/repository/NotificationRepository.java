package com.condotrack.backend.repository;

import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientUser_EmailIgnoreCaseOrderByCreatedAtDesc(
            String email,
            Pageable pageable
    );

    Optional<Notification> findByIdAndRecipientUser_EmailIgnoreCase(
            UUID id,
            String email
    );

    @Query("""
            SELECT COUNT(n)
            FROM Notification n
            WHERE LOWER(n.recipientUser.email) = LOWER(:email)
              AND n.status IN :statuses
            """)
    long countUnreadByRecipientEmail(
            @Param("email") String email,
            @Param("statuses") Collection<Enums.NotificationStatus> statuses
    );

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.status = com.condotrack.backend.model.Enums.NotificationStatus.READ,
                n.readAt = CURRENT_TIMESTAMP,
                n.updatedAt = CURRENT_TIMESTAMP
            WHERE LOWER(n.recipientUser.email) = LOWER(:email)
              AND n.status IN :statuses
            """)
    int markAllReadByRecipientEmail(
            @Param("email") String email,
            @Param("statuses") Collection<Enums.NotificationStatus> statuses
    );
}
