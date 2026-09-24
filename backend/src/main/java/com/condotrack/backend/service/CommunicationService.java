package com.condotrack.backend.service;

import com.condotrack.backend.dto.CommunicationSendRequest;
import com.condotrack.backend.dto.CommunicationSendResponse;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.Notification;
import com.condotrack.backend.model.Role;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.BuildingRepository;
import com.condotrack.backend.repository.NotificationRepository;
import com.condotrack.backend.repository.ResidentRepository;
import com.condotrack.backend.repository.RoleRepository;
import com.condotrack.backend.repository.UnitRepository;
import com.condotrack.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunicationService {

    private static final String ANNOUNCEMENT = "ANNOUNCEMENT";

    private final NotificationRepository notificationRepository;
    private final ResidentRepository residentRepository;
    private final BuildingRepository buildingRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Transactional
    public CommunicationSendResponse send(
            CommunicationSendRequest request,
            Authentication authentication
    ) {
        if (!permissionService.hasPermission(authentication, "COMMUNICATIONS_CREATE")) {
            throw new AccessDeniedException("User is not allowed to create communications");
        }

        String audienceType = normalizeAudience(request.audienceType());
        AudienceTarget target = resolveTarget(audienceType, request);
        User sender = getAuthenticatedUser(authentication);
        OffsetDateTime sentAt = OffsetDateTime.now();

        List<User> recipients = target.recipientIds().stream()
                .map(id -> userRepository.findById(id).orElse(null))
                .filter(user -> user != null && user.isActive())
                .filter(this::isCommunicationRecipient)
                .toList();

        if (recipients.isEmpty()) {
            throw new IllegalStateException("No active resident or owner recipients were found");
        }

        List<Notification> notifications = recipients.stream()
                .map(recipient -> buildNotification(
                        recipient,
                        target.building(),
                        request,
                        sentAt,
                        sender
                ))
                .toList();

        notificationRepository.saveAll(notifications);

        return new CommunicationSendResponse(
                ANNOUNCEMENT,
                audienceType,
                request.subject().trim(),
                notifications.size(),
                sentAt
        );
    }

    private Notification buildNotification(
            User recipient,
            Building building,
            CommunicationSendRequest request,
            OffsetDateTime sentAt,
            User sender
    ) {
        Notification notification = new Notification();
        notification.setBuilding(building);
        notification.setRecipientUser(recipient);
        notification.setNotificationType(ANNOUNCEMENT);
        notification.setStatus(Enums.NotificationStatus.SENT);
        notification.setChannel(Enums.NotificationChannel.IN_APP);
        notification.setSubject(request.subject().trim());
        notification.setMessage(request.message().trim());
        notification.setSentAt(sentAt);
        notification.setUpdatedBy(sender.getId());
        return notification;
    }

    private AudienceTarget resolveTarget(String audienceType, CommunicationSendRequest request) {
        return switch (audienceType) {
            case "ALL_RESIDENTS" -> new AudienceTarget(
                    null,
                    new LinkedHashSet<>(residentRepository.findDistinctActiveRecipientUserIds())
            );
            case "BUILDING_RESIDENTS" -> {
                if (request.buildingId() == null) {
                    throw new IllegalArgumentException("buildingId is required for BUILDING_RESIDENTS");
                }
                Building building = buildingRepository.findByIdAndActiveTrue(request.buildingId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Building not found: " + request.buildingId()
                        ));
                yield new AudienceTarget(
                        building,
                        new LinkedHashSet<>(residentRepository.findDistinctActiveRecipientUserIdsByBuildingId(building.getId()))
                );
            }
            case "UNIT_RESIDENTS" -> {
                if (request.unitId() == null) {
                    throw new IllegalArgumentException("unitId is required for UNIT_RESIDENTS");
                }
                Unit unit = unitRepository.findById(request.unitId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Unit not found: " + request.unitId()
                        ));
                if (!unit.isActive() || unit.getBuilding() == null || !unit.getBuilding().isActive()) {
                    throw new IllegalStateException("Unit or building is inactive");
                }
                yield new AudienceTarget(
                        unit.getBuilding(),
                        new LinkedHashSet<>(residentRepository.findDistinctActiveRecipientUserIdsByUnitId(unit.getId()))
                );
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported communication audience: " + request.audienceType()
            );
        };
    }

    private boolean isCommunicationRecipient(User user) {
        return user.getRoles().stream()
                .map(Role::getCode)
                .anyMatch(role -> "RESIDENT".equals(role) || "OWNER".equals(role));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user is required");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private String normalizeAudience(String audienceType) {
        return audienceType.trim().toUpperCase(Locale.ROOT);
    }

    private record AudienceTarget(Building building, Set<UUID> recipientIds) {
    }
}
