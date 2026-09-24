package com.condotrack.backend.service;

import com.condotrack.backend.dto.NotificationPageResponse;
import com.condotrack.backend.dto.NotificationResponse;
import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.Notification;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.NotificationRepository;
import com.condotrack.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(Authentication authentication, int page, int size) {
        requireViewPermission(authentication);

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE)
        );

        Page<NotificationResponse> notifications = notificationRepository
                .findByRecipientUser_EmailIgnoreCaseOrderByCreatedAtDesc(
                        authentication.getName(), pageable
                )
                .map(this::toResponse);

        return NotificationPageResponse.from(
                notifications,
                unreadCount(authentication)
        );
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotification(UUID notificationId, Authentication authentication) {
        requireViewPermission(authentication);

        return notificationRepository
                .findByIdAndRecipientUser_EmailIgnoreCase(notificationId, authentication.getName())
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification not found: " + notificationId
                ));
    }

    @Transactional(readOnly = true)
    public long unreadCount(Authentication authentication) {
        requireViewPermission(authentication);
        return notificationRepository.countUnreadByRecipientEmail(
                authentication.getName(), unreadStatuses()
        );
    }

    @Transactional
    public NotificationResponse markRead(UUID notificationId, Authentication authentication) {
        requireViewPermission(authentication);

        Notification notification = notificationRepository
                .findByIdAndRecipientUser_EmailIgnoreCase(notificationId, authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification not found: " + notificationId
                ));

        if (notification.getStatus() != Enums.NotificationStatus.READ) {
            User currentUser = getAuthenticatedUser(authentication);
            notification.setStatus(Enums.NotificationStatus.READ);
            notification.setReadAt(OffsetDateTime.now());
            notification.setUpdatedBy(currentUser.getId());
            notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    @Transactional
    public int markAllRead(Authentication authentication) {
        requireViewPermission(authentication);
        return notificationRepository.markAllReadByRecipientEmail(
                authentication.getName(), unreadStatuses()
        );
    }

    private Set<Enums.NotificationStatus> unreadStatuses() {
        return EnumSet.of(
                Enums.NotificationStatus.PENDING,
                Enums.NotificationStatus.SENT,
                Enums.NotificationStatus.DELIVERED
        );
    }

    private void requireViewPermission(Authentication authentication) {
        if (!permissionService.hasPermission(authentication, "COMMUNICATIONS_VIEW")) {
            throw new AccessDeniedException("User is not allowed to view notifications");
        }
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user is required");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private NotificationResponse toResponse(Notification notification) {
        String buildingCode = notification.getBuilding() == null
                ? null
                : notification.getBuilding().getCode();

        return new NotificationResponse(
                notification.getId(),
                notification.getBuilding() == null ? null : notification.getBuilding().getId(),
                buildingCode,
                notification.getNotificationType(),
                notification.getStatus().name(),
                notification.getChannel().name(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getRelatedEntityType(),
                notification.getRelatedEntityId(),
                notification.getSentAt(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
