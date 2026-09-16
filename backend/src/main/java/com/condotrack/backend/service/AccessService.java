package com.condotrack.backend.service;

import com.condotrack.backend.dto.AccessLogResponse;
import com.condotrack.backend.dto.VisitorAuthorizationCreateRequest;
import com.condotrack.backend.dto.VisitorAuthorizationResponse;
import com.condotrack.backend.model.AccessLog;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.Resident;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.model.User;
import com.condotrack.backend.model.Visitor;
import com.condotrack.backend.model.VisitorAuthorization;
import com.condotrack.backend.repository.AccessLogRepository;
import com.condotrack.backend.repository.ResidentRepository;
import com.condotrack.backend.repository.UnitRepository;
import com.condotrack.backend.repository.UserRepository;
import com.condotrack.backend.repository.VisitorAuthorizationRepository;
import com.condotrack.backend.repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessService {

    private final VisitorRepository visitorRepository;
    private final VisitorAuthorizationRepository visitorAuthorizationRepository;
    private final AccessLogRepository accessLogRepository;
    private final ResidentRepository residentRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Transactional
    public VisitorAuthorizationResponse createAuthorization(
            VisitorAuthorizationCreateRequest request,
            Authentication authentication
    ) {
        User authenticatedUser = getAuthenticatedUser(authentication);
        Unit unit = getActiveUnit(request.unitId());
        Building building = unit.getBuilding();

        Resident resident = resolveResidentForAuthorization(
                request.residentId(),
                unit,
                authenticatedUser,
                authentication
        );

        validateDates(request.validFrom(), request.validUntil());

        Visitor visitor = new Visitor();
        visitor.setFirstName(request.visitorFirstName().trim());
        visitor.setLastName(request.visitorLastName().trim());
        visitor.setDocumentType(normalizeOptional(request.documentType()));
        visitor.setDocumentNumber(normalizeOptional(request.documentNumber()));
        visitor.setPhone(normalizeOptional(request.phone()));
        visitor.setCompanyName(normalizeOptional(request.companyName()));
        visitor.setNotes(null);
        visitor.setUpdatedBy(authenticatedUser.getId());
        visitor = visitorRepository.save(visitor);

        VisitorAuthorization authorization = new VisitorAuthorization();
        authorization.setBuilding(building);
        authorization.setUnit(unit);
        authorization.setResident(resident);
        authorization.setVisitor(visitor);
        authorization.setQrToken(generateUniqueQrToken());
        authorization.setStatus(Enums.VisitorAuthorizationStatus.APPROVED);
        authorization.setValidFrom(request.validFrom());
        authorization.setValidUntil(request.validUntil());
        authorization.setPurpose(normalizeOptional(request.purpose()));
        authorization.setApprovedBy(authenticatedUser);
        authorization.setApprovedAt(OffsetDateTime.now());
        authorization.setUpdatedBy(authenticatedUser.getId());

        return toAuthorizationResponse(
                visitorAuthorizationRepository.save(authorization)
        );
    }

    @Transactional
    public AccessLogResponse checkIn(
            UUID authorizationId,
            Authentication authentication
    ) {
        User authenticatedUser = getAuthenticatedUser(authentication);

        VisitorAuthorization authorization = visitorAuthorizationRepository
                .findForAccessOperation(authorizationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Visitor authorization not found: " + authorizationId
                ));

        if (!Enums.VisitorAuthorizationStatus.APPROVED.equals(authorization.getStatus())) {
            throw new IllegalStateException(
                    "Only an APPROVED visitor authorization can be checked in"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        if (now.isBefore(authorization.getValidFrom())
                || !now.isBefore(authorization.getValidUntil())) {
            throw new IllegalStateException(
                    "Visitor authorization is not valid at the current time"
            );
        }

        AccessLog accessLog = new AccessLog();
        accessLog.setBuilding(authorization.getBuilding());
        accessLog.setUnit(authorization.getUnit());
        accessLog.setVisitor(authorization.getVisitor());
        accessLog.setAuthorization(authorization);
        accessLog.setHandledByStaff(null);
        accessLog.setDirection(Enums.AccessDirection.IN);
        accessLog.setAccessMethod(Enums.AccessMethod.MANUAL);
        accessLog.setOccurredAt(now);
        accessLog.setUpdatedBy(authenticatedUser.getId());

        authorization.setStatus(Enums.VisitorAuthorizationStatus.USED);
        authorization.setUpdatedBy(authenticatedUser.getId());

        accessLog = accessLogRepository.save(accessLog);
        visitorAuthorizationRepository.save(authorization);

        return toAccessLogResponse(accessLog);
    }

    private Resident resolveResidentForAuthorization(
            UUID requestedResidentId,
            Unit unit,
            User authenticatedUser,
            Authentication authentication
    ) {
        if (permissionService.hasPermission(authentication, "ACCESS_CREATE")) {
            if (requestedResidentId == null) {
                return null;
            }

            Resident resident = residentRepository.findById(requestedResidentId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Resident not found: " + requestedResidentId
                    ));

            if (!resident.isActive() || !resident.getUnit().getId().equals(unit.getId())) {
                throw new IllegalArgumentException(
                        "Resident must be active and assigned to the requested unit"
                );
            }

            return resident;
        }

        if (!permissionService.hasPermission(authentication, "ACCESS_CREATE_OWN")) {
            throw new IllegalStateException(
                    "User is not allowed to create visitor authorizations"
            );
        }

        return residentRepository.findByUnitIdAndActiveTrue(unit.getId()).stream()
                .filter(resident -> resident.getUser().getId().equals(authenticatedUser.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user is not an active resident of the requested unit"
                ));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user is required");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private Unit getActiveUnit(UUID unitId) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unit not found: " + unitId
                ));

        if (!unit.isActive()) {
            throw new IllegalArgumentException(
                    "Unit is inactive: " + unitId
            );
        }

        if (!unit.getBuilding().isActive()) {
            throw new IllegalArgumentException(
                    "The unit belongs to an inactive building: " + unitId
            );
        }

        return unit;
    }

    private void validateDates(OffsetDateTime validFrom, OffsetDateTime validUntil) {
        if (!validUntil.isAfter(validFrom)) {
            throw new IllegalArgumentException(
                    "validUntil must be after validFrom"
            );
        }
    }

    private String generateUniqueQrToken() {
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (visitorAuthorizationRepository.existsByQrToken(token));
        return token;
    }

    private VisitorAuthorizationResponse toAuthorizationResponse(
            VisitorAuthorization authorization
    ) {
        return new VisitorAuthorizationResponse(
                authorization.getId(),
                authorization.getBuilding().getId(),
                authorization.getUnit().getId(),
                authorization.getUnit().getUnitNumber(),
                authorization.getResident() == null ? null : authorization.getResident().getId(),
                authorization.getVisitor().getId(),
                authorization.getVisitor().getFirstName(),
                authorization.getVisitor().getLastName(),
                authorization.getStatus().name(),
                authorization.getQrToken(),
                authorization.getValidFrom(),
                authorization.getValidUntil(),
                authorization.getPurpose()
        );
    }

    private AccessLogResponse toAccessLogResponse(AccessLog accessLog) {
        return new AccessLogResponse(
                accessLog.getId(),
                accessLog.getBuilding().getId(),
                accessLog.getUnit().getId(),
                accessLog.getUnit().getUnitNumber(),
                accessLog.getVisitor() == null ? null : accessLog.getVisitor().getId(),
                accessLog.getAuthorization() == null ? null : accessLog.getAuthorization().getId(),
                accessLog.getVisitor() == null ? null : accessLog.getVisitor().getFirstName(),
                accessLog.getVisitor() == null ? null : accessLog.getVisitor().getLastName(),
                accessLog.getDirection().name(),
                accessLog.getAccessMethod().name(),
                accessLog.getOccurredAt()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
