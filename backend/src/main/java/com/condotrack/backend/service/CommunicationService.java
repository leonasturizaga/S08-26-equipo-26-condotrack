//-------------------- milestone 19 ---------------------   
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.CommunicationSendRequest;
// import com.condotrack.backend.dto.CommunicationSendResponse;
// import com.condotrack.backend.model.Building;
// import com.condotrack.backend.model.Enums;
// import com.condotrack.backend.model.Notification;
// import com.condotrack.backend.model.Role;
// import com.condotrack.backend.model.Unit;
// import com.condotrack.backend.model.User;
// import com.condotrack.backend.repository.BuildingRepository;
// import com.condotrack.backend.repository.NotificationRepository;
// import com.condotrack.backend.repository.ResidentRepository;
// import com.condotrack.backend.repository.RoleRepository;
// import com.condotrack.backend.repository.UnitRepository;
// import com.condotrack.backend.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.access.AccessDeniedException;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.OffsetDateTime;
// import java.util.LinkedHashSet;
// import java.util.List;
// import java.util.Locale;
// import java.util.Set;
// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// public class CommunicationService {

//     private static final String ANNOUNCEMENT = "ANNOUNCEMENT";

//     private final NotificationRepository notificationRepository;
//     private final ResidentRepository residentRepository;
//     private final BuildingRepository buildingRepository;
//     private final UnitRepository unitRepository;
//     private final UserRepository userRepository;
//     private final PermissionService permissionService;

//     @Transactional
//     public CommunicationSendResponse send(
//             CommunicationSendRequest request,
//             Authentication authentication
//     ) {
//         if (!permissionService.hasPermission(authentication, "COMMUNICATIONS_CREATE")) {
//             throw new AccessDeniedException("User is not allowed to create communications");
//         }

//         String audienceType = normalizeAudience(request.audienceType());
//         AudienceTarget target = resolveTarget(audienceType, request);
//         User sender = getAuthenticatedUser(authentication);
//         OffsetDateTime sentAt = OffsetDateTime.now();

//         List<User> recipients = target.recipientIds().stream()
//                 .map(id -> userRepository.findById(id).orElse(null))
//                 .filter(user -> user != null && user.isActive())
//                 .filter(this::isCommunicationRecipient)
//                 .toList();

//         if (recipients.isEmpty()) {
//             throw new IllegalStateException("No active resident or owner recipients were found");
//         }

//         List<Notification> notifications = recipients.stream()
//                 .map(recipient -> buildNotification(
//                         recipient,
//                         target.building(),
//                         request,
//                         sentAt,
//                         sender
//                 ))
//                 .toList();

//         notificationRepository.saveAll(notifications);

//         return new CommunicationSendResponse(
//                 ANNOUNCEMENT,
//                 audienceType,
//                 request.subject().trim(),
//                 notifications.size(),
//                 sentAt
//         );
//     }

//     private Notification buildNotification(
//             User recipient,
//             Building building,
//             CommunicationSendRequest request,
//             OffsetDateTime sentAt,
//             User sender
//     ) {
//         Notification notification = new Notification();
//         notification.setBuilding(building);
//         notification.setRecipientUser(recipient);
//         notification.setNotificationType(ANNOUNCEMENT);
//         notification.setStatus(Enums.NotificationStatus.SENT);
//         notification.setChannel(Enums.NotificationChannel.IN_APP);
//         notification.setSubject(request.subject().trim());
//         notification.setMessage(request.message().trim());
//         notification.setSentAt(sentAt);
//         notification.setUpdatedBy(sender.getId());
//         return notification;
//     }

//     private AudienceTarget resolveTarget(String audienceType, CommunicationSendRequest request) {
//         return switch (audienceType) {
//             case "ALL_RESIDENTS" -> new AudienceTarget(
//                     null,
//                     new LinkedHashSet<>(residentRepository.findDistinctActiveRecipientUserIds())
//             );
//             case "BUILDING_RESIDENTS" -> {
//                 if (request.buildingId() == null) {
//                     throw new IllegalArgumentException("buildingId is required for BUILDING_RESIDENTS");
//                 }
//                 Building building = buildingRepository.findByIdAndActiveTrue(request.buildingId())
//                         .orElseThrow(() -> new IllegalArgumentException(
//                                 "Building not found: " + request.buildingId()
//                         ));
//                 yield new AudienceTarget(
//                         building,
//                         new LinkedHashSet<>(residentRepository.findDistinctActiveRecipientUserIdsByBuildingId(building.getId()))
//                 );
//             }
//             case "UNIT_RESIDENTS" -> {
//                 if (request.unitId() == null) {
//                     throw new IllegalArgumentException("unitId is required for UNIT_RESIDENTS");
//                 }
//                 Unit unit = unitRepository.findById(request.unitId())
//                         .orElseThrow(() -> new IllegalArgumentException(
//                                 "Unit not found: " + request.unitId()
//                         ));
//                 if (!unit.isActive() || unit.getBuilding() == null || !unit.getBuilding().isActive()) {
//                     throw new IllegalStateException("Unit or building is inactive");
//                 }
//                 yield new AudienceTarget(
//                         unit.getBuilding(),
//                         new LinkedHashSet<>(residentRepository.findDistinctActiveRecipientUserIdsByUnitId(unit.getId()))
//                 );
//             }
//             default -> throw new IllegalArgumentException(
//                     "Unsupported communication audience: " + request.audienceType()
//             );
//         };
//     }

//     private boolean isCommunicationRecipient(User user) {
//         return user.getRoles().stream()
//                 .map(Role::getCode)
//                 .anyMatch(role -> "RESIDENT".equals(role) || "OWNER".equals(role));
//     }

//     private User getAuthenticatedUser(Authentication authentication) {
//         if (authentication == null || authentication.getName() == null) {
//             throw new IllegalStateException("Authenticated user is required");
//         }

//         return userRepository.findByEmailIgnoreCase(authentication.getName())
//                 .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
//     }

//     private String normalizeAudience(String audienceType) {
//         return audienceType.trim().toUpperCase(Locale.ROOT);
//     }

//     private record AudienceTarget(Building building, Set<UUID> recipientIds) {
//     }
// }


//-------------------- milestone 19.1 ---------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.CommunicationSendRequest;
import com.condotrack.backend.dto.CommunicationSendResponse;
import com.condotrack.backend.dto.CommunicationPageResponse;
import com.condotrack.backend.dto.CommunicationResponse;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.Communication;
import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.Notification;
import com.condotrack.backend.model.Role;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.BuildingRepository;
import com.condotrack.backend.repository.NotificationRepository;
import com.condotrack.backend.repository.CommunicationRepository;
import com.condotrack.backend.repository.ResidentRepository;
import com.condotrack.backend.repository.RoleRepository;
import com.condotrack.backend.repository.UnitRepository;
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
    private final CommunicationRepository communicationRepository;
    private final ResidentRepository residentRepository;
    private final BuildingRepository buildingRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Transactional(readOnly = true)
    public CommunicationPageResponse getCommunications(
            Authentication authentication, int page, int size
    ) {
        requireAdmin(authentication);
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100)
        );
        Page<CommunicationResponse> communications = communicationRepository
                .findAllByOrderBySentAtDesc(pageable)
                .map(this::toResponse);
        return CommunicationPageResponse.from(communications);
    }

    @Transactional(readOnly = true)
    public CommunicationResponse getCommunication(UUID communicationId, Authentication authentication) {
        requireAdmin(authentication);
        Communication communication = communicationRepository.findById(communicationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Communication not found: " + communicationId
                ));
        return toResponse(communication);
    }

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

        Communication communication = new Communication();
        communication.setSentByUser(sender);
        communication.setBuilding(target.building());
        communication.setUnit(target.unit());
        communication.setAudienceType(audienceType);
        communication.setSubject(request.subject().trim());
        communication.setMessage(request.message().trim());
        communication.setSentAt(sentAt);
        communication.setUpdatedBy(sender.getId());
        communicationRepository.save(communication);

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
                        sender,
                        communication
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
            User sender,
            Communication communication
    ) {
        Notification notification = new Notification();
        notification.setBuilding(building);
        notification.setCommunication(communication);
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
                        null,
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
                        unit,
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

    private CommunicationResponse toResponse(Communication communication) {
        long recipientCount = notificationRepository.countByCommunication_Id(communication.getId());
        long readCount = notificationRepository.countByCommunication_IdAndStatus(
                communication.getId(), Enums.NotificationStatus.READ
        );
        String buildingCode = communication.getBuilding() == null ? null : communication.getBuilding().getCode();
        String unitNumber = communication.getUnit() == null ? null : communication.getUnit().getUnitNumber();
        String senderName = communication.getSentByUser() == null
                ? null
                : (communication.getSentByUser().getFirstName() + " " + communication.getSentByUser().getLastName()).trim();
        return new CommunicationResponse(
                communication.getId(),
                communication.getSubject(),
                communication.getMessage(),
                communication.getAudienceType(),
                communication.getBuilding() == null ? null : communication.getBuilding().getId(),
                buildingCode,
                communication.getUnit() == null ? null : communication.getUnit().getId(),
                unitNumber,
                communication.getSentByUser() == null ? null : communication.getSentByUser().getId(),
                senderName,
                communication.getSentAt(),
                recipientCount,
                readCount,
                Math.max(recipientCount - readCount, 0)
        );
    }

    private void requireAdmin(Authentication authentication) {
        if (!permissionService.hasPermission(authentication, "COMMUNICATIONS_VIEW")) {
            throw new AccessDeniedException("User is not allowed to view sent communications");
        }

        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Authenticated user is required");
        }

        Set<String> roleCodes = userRepository.findRoleCodesByEmailIgnoreCase(authentication.getName());
        if (!roleCodes.contains("ADMINISTRATOR")) {
            throw new AccessDeniedException("Administrator role is required");
        }
    }

    private record AudienceTarget(Building building, Unit unit, Set<UUID> recipientIds) {
    }
}
