package com.condotrack.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor
public class Notification extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "building_id") private Building building;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "recipient_user_id", nullable = false) private User recipientUser;
    @Column(name = "notification_type", nullable = false, length = 40) private String notificationType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Enums.NotificationStatus status = Enums.NotificationStatus.PENDING;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Enums.NotificationChannel channel = Enums.NotificationChannel.IN_APP;
    @Column(length = 255) private String subject;
    @Column(nullable = false, columnDefinition = "TEXT") private String message;
    @Column(name = "related_entity_type", length = 80) private String relatedEntityType;
    @Column(name = "related_entity_id") private UUID relatedEntityId;
    @Column(name = "sent_at") private OffsetDateTime sentAt;
    @Column(name = "read_at") private OffsetDateTime readAt;
}
