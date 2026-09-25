//--------------- milestone 14.1 ---------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.AccessLogResponse;
// import com.condotrack.backend.dto.VisitorAuthorizationCreateRequest;
// import com.condotrack.backend.dto.VisitorAuthorizationPageResponse;
// import com.condotrack.backend.dto.VisitorAuthorizationResponse;
// import com.condotrack.backend.model.AccessLog;
// import com.condotrack.backend.model.Building;
// import com.condotrack.backend.model.Enums;
// import com.condotrack.backend.model.Resident;
// import com.condotrack.backend.model.Staff;
// import com.condotrack.backend.model.Unit;
// import com.condotrack.backend.model.User;
// import com.condotrack.backend.model.Visitor;
// import com.condotrack.backend.model.VisitorAuthorization;
// import com.condotrack.backend.repository.AccessLogRepository;
// import com.condotrack.backend.repository.ResidentRepository;
// import com.condotrack.backend.repository.StaffRepository;
// import com.condotrack.backend.repository.UnitRepository;
// import com.condotrack.backend.repository.UserRepository;
// import com.condotrack.backend.repository.VisitorAuthorizationRepository;
// import com.condotrack.backend.repository.VisitorRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.OffsetDateTime;
// import java.util.Collection;
// import java.util.List;
// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// public class AccessService {

//     private static final int DEFAULT_PAGE_SIZE = 20;
//     private static final int MAX_PAGE_SIZE = 100;

//     private final VisitorRepository visitorRepository;
//     private final VisitorAuthorizationRepository visitorAuthorizationRepository;
//     private final AccessLogRepository accessLogRepository;
//     private final ResidentRepository residentRepository;
//     private final UnitRepository unitRepository;
//     private final UserRepository userRepository;
//     private final StaffRepository staffRepository;
//     private final PermissionService permissionService;

//     @Transactional
//     public VisitorAuthorizationResponse createAuthorization(
//             VisitorAuthorizationCreateRequest request,
//             Authentication authentication
//     ) {
//         User authenticatedUser = getAuthenticatedUser(authentication);
//         Unit unit = getActiveUnit(request.unitId());
//         Building building = unit.getBuilding();

//         Resident resident = resolveResidentForAuthorization(
//                 request.residentId(),
//                 unit,
//                 authenticatedUser,
//                 authentication
//         );

//         validateDates(request.validFrom(), request.validUntil());

//         Visitor visitor = new Visitor();
//         visitor.setFirstName(request.visitorFirstName().trim());
//         visitor.setLastName(request.visitorLastName().trim());
//         visitor.setDocumentType(normalizeOptional(request.documentType()));
//         visitor.setDocumentNumber(normalizeOptional(request.documentNumber()));
//         visitor.setPhone(normalizeOptional(request.phone()));
//         visitor.setCompanyName(normalizeOptional(request.companyName()));
//         visitor.setNotes(null);
//         visitor.setUpdatedBy(authenticatedUser.getId());
//         visitor = visitorRepository.save(visitor);

//         VisitorAuthorization authorization = new VisitorAuthorization();
//         authorization.setBuilding(building);
//         authorization.setUnit(unit);
//         authorization.setResident(resident);
//         authorization.setVisitor(visitor);
//         authorization.setQrToken(generateUniqueQrToken());
//         authorization.setStatus(Enums.VisitorAuthorizationStatus.APPROVED);
//         authorization.setValidFrom(request.validFrom());
//         authorization.setValidUntil(request.validUntil());
//         authorization.setPurpose(normalizeOptional(request.purpose()));
//         authorization.setApprovedBy(authenticatedUser);
//         authorization.setApprovedAt(OffsetDateTime.now());
//         authorization.setUpdatedBy(authenticatedUser.getId());

//         return toAuthorizationResponse(
//                 visitorAuthorizationRepository.save(authorization)
//         );
//     }

//     @Transactional(readOnly = true)
//     public VisitorAuthorizationPageResponse listAuthorizations(
//             Authentication authentication,
//             Pageable pageable,
//             String status
//     ) {
//         Pageable safePageable = normalizePageable(pageable, "createdAt");
//         Enums.VisitorAuthorizationStatus requestedStatus = parseOptionalStatus(status);
//         Page<VisitorAuthorization> page;

//         if (permissionService.hasPermission(authentication, "ACCESS_VIEW")) {
//             page = findAuthorizationPage(safePageable, requestedStatus, List.of());
//         } else if (permissionService.hasPermission(authentication, "ACCESS_VIEW_OWN")) {
//             List<UUID> unitIds = getOwnActiveUnitIds(authentication);
//             if (unitIds.isEmpty()) {
//                 page = Page.empty(safePageable);
//             } else {
//                 page = findAuthorizationPage(safePageable, requestedStatus, unitIds);
//             }
//         } else {
//             throw new org.springframework.security.access.AccessDeniedException(
//                     "You do not have permission to view visitor authorizations"
//             );
//         }

//         Page<VisitorAuthorizationResponse> responsePage = page.map(this::toAuthorizationResponse);
//         return VisitorAuthorizationPageResponse.from(responsePage);
//     }

//     @Transactional(readOnly = true)
//     public VisitorAuthorizationResponse getAuthorization(UUID authorizationId) {
//         VisitorAuthorization authorization = visitorAuthorizationRepository
//                 .findForAccessOperation(authorizationId)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Visitor authorization not found: " + authorizationId
//                 ));

//         return toAuthorizationResponse(authorization);
//     }

//     @Transactional
//     public AccessLogResponse checkIn(
//             UUID authorizationId,
//             Authentication authentication
//     ) {
//         User authenticatedUser = getAuthenticatedUser(authentication);

//         VisitorAuthorization authorization = visitorAuthorizationRepository
//                 .findForAccessOperation(authorizationId)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Visitor authorization not found: " + authorizationId
//                 ));

//         return checkInAuthorization(authorization, authenticatedUser, Enums.AccessMethod.MANUAL);
//     }

//     @Transactional
//     public AccessLogResponse checkInByQrToken(
//             String qrToken,
//             Authentication authentication
//     ) {
//         User authenticatedUser = getAuthenticatedUser(authentication);
//         String normalizedToken = normalizeRequired(qrToken);

//         VisitorAuthorization authorization = visitorAuthorizationRepository
//                 .findForAccessOperationByQrToken(normalizedToken)
//                 .orElseThrow(() -> new IllegalArgumentException("Visitor authorization not found"));

//         return checkInAuthorization(authorization, authenticatedUser, Enums.AccessMethod.QR);
//     }

//     @Transactional
//     public AccessLogResponse checkOut(
//             UUID authorizationId,
//             Authentication authentication
//     ) {
//         User authenticatedUser = getAuthenticatedUser(authentication);

//         VisitorAuthorization authorization = visitorAuthorizationRepository
//                 .findForAccessOperation(authorizationId)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Visitor authorization not found: " + authorizationId
//                 ));

//         AccessLog latestLog = accessLogRepository
//                 .findTopByAuthorization_IdOrderByOccurredAtDesc(authorizationId)
//                 .orElseThrow(() -> new IllegalStateException(
//                         "Visitor has no registered entry for this authorization"
//                 ));

//         if (!Enums.AccessDirection.IN.equals(latestLog.getDirection())) {
//             throw new IllegalStateException("Visitor is not currently inside");
//         }

//         return createExitLog(authorization, authenticatedUser);
//     }

//     @Transactional
//     public AccessLogResponse checkOutByQrToken(
//             String qrToken,
//             Authentication authentication
//     ) {
//         User authenticatedUser = getAuthenticatedUser(authentication);
//         String normalizedToken = normalizeRequired(qrToken);

//         VisitorAuthorization authorization = visitorAuthorizationRepository
//                 .findForAccessOperationByQrToken(normalizedToken)
//                 .orElseThrow(() -> new IllegalArgumentException("Visitor authorization not found"));

//         AccessLog latestLog = accessLogRepository
//                 .findTopByAuthorization_IdOrderByOccurredAtDesc(authorization.getId())
//                 .orElseThrow(() -> new IllegalStateException(
//                         "Visitor has no registered entry for this authorization"
//                 ));

//         if (!Enums.AccessDirection.IN.equals(latestLog.getDirection())) {
//             throw new IllegalStateException("Visitor is not currently inside");
//         }

//         return createExitLog(authorization, authenticatedUser);
//     }

//     @Transactional(readOnly = true)
//     public Page<AccessLogResponse> listActiveVisitors(
//             Authentication authentication,
//             Pageable pageable
//     ) {
//         Pageable safePageable = normalizePageable(pageable, "occurredAt");
//         Page<AccessLog> page;

//         if (permissionService.hasPermission(authentication, "ACCESS_VIEW")) {
//             page = accessLogRepository.findActiveVisitorEntries(
//                     Enums.AccessDirection.IN,
//                     safePageable
//             );
//         } else if (permissionService.hasPermission(authentication, "ACCESS_VIEW_OWN")) {
//             List<UUID> unitIds = getOwnActiveUnitIds(authentication);
//             if (unitIds.isEmpty()) {
//                 return Page.empty(safePageable);
//             }

//             page = accessLogRepository.findActiveVisitorEntriesForUnits(
//                     Enums.AccessDirection.IN,
//                     unitIds,
//                     safePageable
//             );
//         } else {
//             throw new org.springframework.security.access.AccessDeniedException(
//                     "You do not have permission to view active visitors"
//             );
//         }

//         return page.map(this::toAccessLogResponse);
//     }

//     private AccessLogResponse createExitLog(
//             VisitorAuthorization authorization,
//             User authenticatedUser
//     ) {
//         OffsetDateTime now = OffsetDateTime.now();
//         AccessLog accessLog = new AccessLog();
//         accessLog.setBuilding(authorization.getBuilding());
//         accessLog.setUnit(authorization.getUnit());
//         accessLog.setVisitor(authorization.getVisitor());
//         accessLog.setAuthorization(authorization);
//         accessLog.setHandledByStaff(resolveStaff(authenticatedUser, authorization.getBuilding().getId()));
//         accessLog.setDirection(Enums.AccessDirection.OUT);
//         accessLog.setAccessMethod(Enums.AccessMethod.MANUAL);
//         accessLog.setOccurredAt(now);
//         accessLog.setUpdatedBy(authenticatedUser.getId());

//         return toAccessLogResponse(accessLogRepository.save(accessLog));
//     }

//     private AccessLogResponse checkInAuthorization(
//             VisitorAuthorization authorization,
//             User authenticatedUser,
//             Enums.AccessMethod accessMethod
//     ) {
//         if (!Enums.VisitorAuthorizationStatus.APPROVED.equals(authorization.getStatus())) {
//             throw new IllegalStateException(
//                     "Only an APPROVED visitor authorization can be checked in"
//             );
//         }

//         OffsetDateTime now = OffsetDateTime.now();

//         if (now.isBefore(authorization.getValidFrom())
//                 || !now.isBefore(authorization.getValidUntil())) {
//             throw new IllegalStateException(
//                     "Visitor authorization is not valid at the current time"
//             );
//         }

//         AccessLog existing = accessLogRepository
//                 .findTopByAuthorization_IdOrderByOccurredAtDesc(authorization.getId())
//                 .orElse(null);

//         if (existing != null && Enums.AccessDirection.IN.equals(existing.getDirection())) {
//             throw new IllegalStateException("Visitor is already checked in");
//         }

//         AccessLog accessLog = new AccessLog();
//         accessLog.setBuilding(authorization.getBuilding());
//         accessLog.setUnit(authorization.getUnit());
//         accessLog.setVisitor(authorization.getVisitor());
//         accessLog.setAuthorization(authorization);
//         accessLog.setHandledByStaff(resolveStaff(authenticatedUser, authorization.getBuilding().getId()));
//         accessLog.setDirection(Enums.AccessDirection.IN);
//         accessLog.setAccessMethod(accessMethod);
//         accessLog.setOccurredAt(now);
//         accessLog.setUpdatedBy(authenticatedUser.getId());

//         authorization.setStatus(Enums.VisitorAuthorizationStatus.USED);
//         authorization.setUpdatedBy(authenticatedUser.getId());

//         accessLog = accessLogRepository.save(accessLog);
//         visitorAuthorizationRepository.save(authorization);

//         return toAccessLogResponse(accessLog);
//     }

//     private Staff resolveStaff(User authenticatedUser, UUID buildingId) {
//         return staffRepository.findFirstByUser_IdAndBuilding_IdAndActiveTrue(
//                 authenticatedUser.getId(),
//                 buildingId
//         ).orElse(null);
//     }

//     private Page<VisitorAuthorization> findAuthorizationPage(
//             Pageable pageable,
//             Enums.VisitorAuthorizationStatus requestedStatus,
//             Collection<UUID> ownUnitIds
//     ) {
//         boolean ownScope = ownUnitIds != null && !ownUnitIds.isEmpty();

//         if (requestedStatus == Enums.VisitorAuthorizationStatus.EXPIRED) {
//             if (ownScope) {
//                 return visitorAuthorizationRepository.findApprovedExpiredForUnits(
//                         ownUnitIds,
//                         Enums.VisitorAuthorizationStatus.APPROVED,
//                         OffsetDateTime.now(),
//                         pageable
//                 );
//             }

//             return visitorAuthorizationRepository.findApprovedExpired(
//                     Enums.VisitorAuthorizationStatus.APPROVED,
//                     OffsetDateTime.now(),
//                     pageable
//             );
//         }

//         if (requestedStatus != null) {
//             if (ownScope) {
//                 return visitorAuthorizationRepository.findByUnitIdInAndStatusOrderByCreatedAtDesc(
//                         ownUnitIds,
//                         requestedStatus,
//                         pageable
//                 );
//             }

//             return visitorAuthorizationRepository.findByStatusOrderByCreatedAtDesc(
//                     requestedStatus,
//                     pageable
//             );
//         }

//         if (ownScope) {
//             return visitorAuthorizationRepository.findByUnitIdInOrderByCreatedAtDesc(
//                     ownUnitIds,
//                     pageable
//             );
//         }

//         return visitorAuthorizationRepository.findAllByOrderByCreatedAtDesc(pageable);
//     }

//     private List<UUID> getOwnActiveUnitIds(Authentication authentication) {
//         return residentRepository.findAllByUser_EmailIgnoreCaseAndActiveTrue(authentication.getName())
//                 .stream()
//                 .map(resident -> resident.getUnit().getId())
//                 .distinct()
//                 .toList();
//     }

//     private Resident resolveResidentForAuthorization(
//             UUID requestedResidentId,
//             Unit unit,
//             User authenticatedUser,
//             Authentication authentication
//     ) {
//         if (permissionService.hasPermission(authentication, "ACCESS_CREATE")) {
//             if (requestedResidentId == null) {
//                 return null;
//             }

//             Resident resident = residentRepository.findById(requestedResidentId)
//                     .orElseThrow(() -> new IllegalArgumentException(
//                             "Resident not found: " + requestedResidentId
//                     ));

//             if (!resident.isActive() || !resident.getUnit().getId().equals(unit.getId())) {
//                 throw new IllegalArgumentException(
//                         "Resident must be active and assigned to the requested unit"
//                 );
//             }

//             return resident;
//         }

//         if (!permissionService.hasPermission(authentication, "ACCESS_CREATE_OWN")) {
//             throw new IllegalStateException(
//                     "User is not allowed to create visitor authorizations"
//             );
//         }

//         return residentRepository.findByUnitIdAndActiveTrue(unit.getId()).stream()
//                 .filter(resident -> resident.getUser().getId().equals(authenticatedUser.getId()))
//                 .findFirst()
//                 .orElseThrow(() -> new IllegalStateException(
//                         "Authenticated user is not an active resident of the requested unit"
//                 ));
//     }

//     private User getAuthenticatedUser(Authentication authentication) {
//         if (authentication == null || authentication.getName() == null) {
//             throw new IllegalStateException("Authenticated user is required");
//         }

//         return userRepository.findByEmailIgnoreCase(authentication.getName())
//                 .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
//     }

//     private Unit getActiveUnit(UUID unitId) {
//         Unit unit = unitRepository.findById(unitId)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Unit not found: " + unitId
//                 ));

//         if (!unit.isActive()) {
//             throw new IllegalArgumentException(
//                     "Unit is inactive: " + unitId
//             );
//         }

//         if (!unit.getBuilding().isActive()) {
//             throw new IllegalArgumentException(
//                     "The unit belongs to an inactive building: " + unitId
//             );
//         }

//         return unit;
//     }

//     private void validateDates(OffsetDateTime validFrom, OffsetDateTime validUntil) {
//         if (!validUntil.isAfter(validFrom)) {
//             throw new IllegalArgumentException(
//                     "validUntil must be after validFrom"
//             );
//         }
//     }

//     private String generateUniqueQrToken() {
//         String token;
//         do {
//             token = UUID.randomUUID().toString();
//         } while (visitorAuthorizationRepository.existsByQrToken(token));
//         return token;
//     }

//     private VisitorAuthorizationResponse toAuthorizationResponse(
//             VisitorAuthorization authorization
//     ) {
//         String effectiveStatus = authorization.getStatus().name();
//         if (Enums.VisitorAuthorizationStatus.APPROVED.equals(authorization.getStatus())
//                 && !OffsetDateTime.now().isBefore(authorization.getValidUntil())) {
//             effectiveStatus = Enums.VisitorAuthorizationStatus.EXPIRED.name();
//         }

//         return new VisitorAuthorizationResponse(
//                 authorization.getId(),
//                 authorization.getBuilding().getId(),
//                 authorization.getUnit().getId(),
//                 authorization.getUnit().getUnitNumber(),
//                 authorization.getResident() == null ? null : authorization.getResident().getId(),
//                 authorization.getVisitor().getId(),
//                 authorization.getVisitor().getFirstName(),
//                 authorization.getVisitor().getLastName(),
//                 effectiveStatus,
//                 authorization.getQrToken(),
//                 authorization.getValidFrom(),
//                 authorization.getValidUntil(),
//                 authorization.getPurpose(),
//                 authorization.getCreatedAt(),
//                 authorization.getApprovedAt()
//         );
//     }

//     private AccessLogResponse toAccessLogResponse(AccessLog accessLog) {
//         Staff handledByStaff = accessLog.getHandledByStaff();
//         return new AccessLogResponse(
//                 accessLog.getId(),
//                 accessLog.getBuilding().getId(),
//                 accessLog.getUnit().getId(),
//                 accessLog.getUnit().getUnitNumber(),
//                 accessLog.getVisitor() == null ? null : accessLog.getVisitor().getId(),
//                 accessLog.getAuthorization() == null ? null : accessLog.getAuthorization().getId(),
//                 accessLog.getVisitor() == null ? null : accessLog.getVisitor().getFirstName(),
//                 accessLog.getVisitor() == null ? null : accessLog.getVisitor().getLastName(),
//                 accessLog.getDirection().name(),
//                 accessLog.getAccessMethod().name(),
//                 accessLog.getOccurredAt(),
//                 handledByStaff == null ? null : handledByStaff.getId(),
//                 handledByStaff == null ? null : handledByStaff.getUser().getFirstName() + " " + handledByStaff.getUser().getLastName()
//         );
//     }

//     private String normalizeRequired(String value) {
//         if (value == null || value.isBlank()) {
//             throw new IllegalArgumentException("qrToken is required");
//         }
//         return value.trim();
//     }

//     private String normalizeOptional(String value) {
//         if (value == null) {
//             return null;
//         }

//         String normalized = value.trim();
//         return normalized.isEmpty() ? null : normalized;
//     }

//     private Enums.VisitorAuthorizationStatus parseOptionalStatus(String value) {
//         if (value == null || value.isBlank()) {
//             return null;
//         }

//         try {
//             return Enums.VisitorAuthorizationStatus.valueOf(value.trim().toUpperCase());
//         } catch (IllegalArgumentException exception) {
//             throw new IllegalArgumentException("Unknown visitor authorization status: " + value);
//         }
//     }

//     private Pageable normalizePageable(Pageable pageable, String defaultSortProperty) {
//         int page = Math.max(pageable.getPageNumber(), 0);
//         int requestedSize = pageable.getPageSize() > 0
//                 ? pageable.getPageSize()
//                 : DEFAULT_PAGE_SIZE;
//         int size = Math.min(requestedSize, MAX_PAGE_SIZE);
//         Sort sort = Sort.by(Sort.Direction.DESC, defaultSortProperty);

//         return PageRequest.of(page, size, sort);
//     }
// }


//---------------- milestone 20 ---------------------
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
    private final AuditService auditService;

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

        VisitorAuthorization saved = visitorAuthorizationRepository.save(authorization);
        auditService.record(authentication.getName(), building, "VISITOR_AUTHORIZATION", saved.getId(), "CREATE", null, saved.getStatus().name(), null, java.util.Map.of("unitId", unit.getId().toString(), "visitorId", visitor.getId().toString()));
        return toAuthorizationResponse(saved);
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
        auditService.record(authentication.getName(), authorization.getBuilding(), "ACCESS_LOG", accessLog.getId(), "CHECK_IN", null, "IN", null, java.util.Map.of("authorizationId", authorization.getId().toString()));

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
                authorization.getPurpose(),
                authorization.getCreatedAt(),
                authorization.getApprovedAt()
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
                accessLog.getOccurredAt(),
                accessLog.getHandledByStaff() == null ? null : accessLog.getHandledByStaff().getId(),
                accessLog.getHandledByStaff() == null ? null : accessLog.getHandledByStaff().getUser().getFirstName() + " " + accessLog.getHandledByStaff().getUser().getLastName()
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