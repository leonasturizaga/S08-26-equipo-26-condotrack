//--------------- Milestone 18.4 rev4 ----------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.MoveCreateRequest;
import com.condotrack.backend.dto.MoveOwnerAuthorizationRequest;
import com.condotrack.backend.dto.MovePageResponse;
import com.condotrack.backend.dto.MoveResidentOptionResponse;
import com.condotrack.backend.dto.MoveResponse;
import com.condotrack.backend.dto.MoveStatusUpdateRequest;
import com.condotrack.backend.dto.MoveUnitOptionResponse;
import com.condotrack.backend.model.AuditLog;
import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.MoveRequest;
import com.condotrack.backend.model.Resident;
import com.condotrack.backend.model.Staff;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.AuditLogRepository;
import com.condotrack.backend.repository.MoveRequestRepository;
import com.condotrack.backend.repository.ResidentRepository;
import com.condotrack.backend.repository.StaffRepository;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MoveService {
    private final MoveRequestRepository moveRepository;
    private final UnitRepository unitRepository;
    private final ResidentRepository residentRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final AuditLogRepository auditLogRepository;
    private final MoveAccessService moveAccessService;

    @Transactional(readOnly = true)
    public MovePageResponse getMoves(Authentication authentication, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<MoveRequest> result;

        if (hasRole(authentication, "ADMINISTRATOR") && permissionService.hasPermission(authentication, "MOVES_VIEW")) {
            result = moveRepository.findAllByOrderByRequestedAtDesc(pageable);
        } else if (permissionService.hasPermission(authentication, "MOVES_VIEW")) {
            List<UUID> buildingIds = staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
                    .map(staff -> staff.getBuilding().getId())
                    .distinct()
                    .filter(buildingId -> permissionService.hasPermission(authentication, "MOVES_VIEW", buildingId))
                    .toList();
            result = buildingIds.isEmpty()
                    ? Page.empty(pageable)
                    : moveRepository.findByUnit_Building_IdInOrderByRequestedAtDesc(buildingIds, pageable);
        } else if (permissionService.hasPermission(authentication, "MOVES_VIEW_UNIT")) {
            List<UUID> unitIds = residentRepository.findActiveUnitIdsForUser(authentication.getName()).stream()
                    .map(unitId -> unitRepository.findById(unitId).orElse(null))
                    .filter(unit -> unit != null && permissionService.hasPermission(authentication, "MOVES_VIEW_UNIT", unit.getBuilding().getId()))
                    .map(Unit::getId)
                    .toList();
            result = unitIds.isEmpty()
                    ? Page.empty(pageable)
                    : moveRepository.findByUnit_IdInOrderByRequestedAtDesc(unitIds, pageable);
        } else {
            List<UUID> unitIds = residentRepository.findActiveUnitIdsForUser(authentication.getName()).stream()
                    .map(unitId -> unitRepository.findById(unitId).orElse(null))
                    .filter(unit -> unit != null && permissionService.hasPermission(authentication, "MOVES_VIEW_OWN", unit.getBuilding().getId()))
                    .map(Unit::getId)
                    .toList();
            result = unitIds.isEmpty()
                    ? Page.empty(pageable)
                    : moveRepository.findByUnit_IdInOrderByRequestedAtDesc(unitIds, pageable);
        }

        return new MovePageResponse(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public MoveResponse getMove(UUID id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<MoveUnitOptionResponse> getUnitOptions(Authentication authentication) {
        if (hasRole(authentication, "ADMINISTRATOR") && permissionService.hasPermission(authentication, "MOVES_CREATE")) {
            return toUnitOptions(unitRepository.findByActiveTrueOrderByBuildingIdAscUnitNumberAsc());
        }

        if (permissionService.hasPermission(authentication, "MOVES_CREATE")) {
            List<UUID> buildingIds = staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
                    .map(staff -> staff.getBuilding() == null ? null : staff.getBuilding().getId())
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .filter(buildingId -> permissionService.hasPermission(authentication, "MOVES_CREATE", buildingId))
                    .toList();

            return buildingIds.isEmpty()
                    ? List.of()
                    : toUnitOptions(unitRepository.findByBuildingIdInAndActiveTrueOrderByBuildingIdAscUnitNumberAsc(buildingIds));
        }

        if (permissionService.hasPermission(authentication, "MOVES_CREATE_OWN")) {
            return toUnitOptions(unitRepository.findActiveUnitsForUser(
                    authentication.getName(), PageRequest.of(0, 100)).getContent());
        }

        throw new AccessDeniedException("User is not allowed to create move requests");
    }

    @Transactional(readOnly = true)
    public List<MoveResidentOptionResponse> getResidentOptions(UUID unitId, Authentication authentication) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + unitId));
        validateActiveUnit(unit);
        boolean administrator = hasRole(authentication, "ADMINISTRATOR")
                && permissionService.hasPermission(authentication, "MOVES_CREATE");
        boolean own = permissionService.hasPermission(authentication, "MOVES_CREATE_OWN", unit.getBuilding().getId())
                && residentRepository.existsActiveResidentForUserAndUnit(authentication.getName(), unitId);
        boolean assignedStaff = permissionService.hasPermission(authentication, "MOVES_CREATE", unit.getBuilding().getId())
                && staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
                .anyMatch(staff -> staff.getBuilding() != null
                        && staff.getBuilding().getId().equals(unit.getBuilding().getId()));
        if (!administrator && !own && !assignedStaff) {
            throw new AccessDeniedException("User is not allowed to list residents for this unit");
        }
        return residentRepository.findByUnitIdAndActiveTrue(unitId).stream()
                .map(r -> new MoveResidentOptionResponse(
                        r.getId(), r.getUnit().getId(), r.getUnit().getUnitNumber(),
                        r.getUser().getFirstName(), r.getUser().getLastName(),
                        r.getResidentType().name()))
                .toList();
    }

    @Transactional
    public MoveResponse createMove(MoveCreateRequest request, Authentication authentication) {
        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + request.unitId()));
        validateActiveUnit(unit);
        validateCreateScope(authentication, unit.getId());
        validateSchedule(request.scheduledStart(), request.scheduledEnd());

        User currentUser = getAuthenticatedUser(authentication);
        MoveRequest move = new MoveRequest();
        move.setBuilding(unit.getBuilding());
        move.setUnit(unit);
        move.setRequestType(request.requestType());
        move.setRequestedAt(OffsetDateTime.now());
        move.setScheduledStart(request.scheduledStart());
        move.setScheduledEnd(request.scheduledEnd());
        move.setNotes(normalize(request.notes()));
        move.setStatus(Enums.MoveRequestStatus.PENDING_APPROVAL);

        Resident resident;
        // Administrator has unrestricted move-creation scope and must never be
        // forced through the MOVES_CREATE_OWN branch, even if the administrator
        // role also carries that permission in the global permission catalog.
        if (hasRole(authentication, "ADMINISTRATOR")) {
            resident = resolveSelectedResidentForUnit(request.residentId(), unit);
        } else if (permissionService.hasPermission(authentication, "MOVES_CREATE_OWN")) {
            resident = residentRepository.findByUnitIdAndActiveTrue(unit.getId()).stream()
                    .filter(r -> r.getUser().getEmail().equalsIgnoreCase(authentication.getName()))
                    .findFirst()
                    .orElseThrow(() -> new AccessDeniedException("User is not an active resident of the selected unit"));
        } else {
            resident = resolveSelectedResidentForUnit(request.residentId(), unit);
        }

        move.setResident(resident);
        move.setUpdatedBy(currentUser.getId());
        MoveRequest saved = moveRepository.save(move);
        audit(authentication.getName(), saved, "MOVE_CREATED", null, saved.getStatus().name(), Map.of(
                "requestType", saved.getRequestType().name(),
                "unitId", saved.getUnit().getId().toString()
        ));
        return toResponse(saved);
    }

    @Transactional
    public MoveResponse updateOwnerAuthorization(UUID id, MoveOwnerAuthorizationRequest request, Authentication authentication) {
        MoveRequest move = getEntity(id);
        if (move.getStatus() == Enums.MoveRequestStatus.REJECTED
                || move.getStatus() == Enums.MoveRequestStatus.COMPLETED
                || move.getStatus() == Enums.MoveRequestStatus.CANCELLED) {
            throw new IllegalStateException("Owner authorization is not available for a terminal move request");
        }
        if (!moveAccessService.canAuthorizeOwner(authentication, id)) {
            throw new AccessDeniedException("Only the owner of the affected unit can authorize this move request");
        }
        User actor = getAuthenticatedUser(authentication);
        boolean previous = move.isOwnerAuthorized();
        boolean next = Boolean.TRUE.equals(request.authorized());
        move.setOwnerAuthorized(next);
        move.setOwnerAuthorizedByUser(next ? actor : null);
        move.setOwnerAuthorizedAt(next ? OffsetDateTime.now() : null);
        move.setOwnerAuthorizationNotes(normalize(request.notes()));
        move.setUpdatedBy(actor.getId());
        MoveRequest saved = moveRepository.save(move);
        audit(authentication.getName(), saved, next ? "MOVE_OWNER_AUTHORIZED" : "MOVE_OWNER_AUTHORIZATION_REVOKED",
                Boolean.toString(previous), Boolean.toString(next), Map.of("unitId", saved.getUnit().getId().toString()));
        return toResponse(saved);
    }

    @Transactional
    public MoveResponse updateStatus(UUID id, MoveStatusUpdateRequest request, Authentication authentication) {
        MoveRequest move = getEntity(id);
        if (!hasRole(authentication, "ADMINISTRATOR") || !permissionService.hasPermission(authentication, "MOVES_APPROVE", move.getBuilding().getId())) {
            throw new AccessDeniedException("Only administrators can approve or update move request status");
        }
        if (request.status() == null) throw new IllegalArgumentException("status is required");
        Enums.MoveRequestStatus current = move.getStatus();
        Enums.MoveRequestStatus target = request.status();
        validateTransition(current, target);

        if (target == Enums.MoveRequestStatus.APPROVED && requiresOwnerAuthorization(move) && !move.isOwnerAuthorized()) {
            throw new IllegalStateException("Owner authorization is required before this move request can be approved");
        }
        if (target == Enums.MoveRequestStatus.SCHEDULED) {
            OffsetDateTime start = request.scheduledStart() != null ? request.scheduledStart() : move.getScheduledStart();
            OffsetDateTime end = request.scheduledEnd() != null ? request.scheduledEnd() : move.getScheduledEnd();
            validateSchedule(start, end);
            move.setScheduledStart(start);
            move.setScheduledEnd(end);
        }

        User actor = getAuthenticatedUser(authentication);
        move.setStatus(target);
        if (request.notes() != null) move.setNotes(normalize(request.notes()));
        move.setUpdatedBy(actor.getId());

        if (target == Enums.MoveRequestStatus.APPROVED) {
            staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
                    .filter(staff -> staff.getBuilding().getId().equals(move.getBuilding().getId()))
                    .findFirst()
                    .ifPresent(staff -> {
                        move.setApprovedByStaff(staff);
                        move.setApprovedAt(OffsetDateTime.now());
                    });
        }
        MoveRequest saved = moveRepository.save(move);
        audit(authentication.getName(), saved, "MOVE_STATUS_CHANGED", current.name(), target.name(), Map.of());
        return toResponse(saved);
    }

    private void validateCreateScope(Authentication authentication, UUID unitId) {
        // Administrator is the global exception: MOVES_CREATE is sufficient and
        // building-level overrides are intentionally ignored by PermissionService.
        // This check must happen before MOVES_CREATE_OWN because administrators may
        // also inherit OWN permissions from the global permission catalog.
        if (hasRole(authentication, "ADMINISTRATOR")) {
            if (!permissionService.hasPermission(authentication, "MOVES_CREATE")) {
                throw new AccessDeniedException("Administrator is not allowed to create move requests");
            }
            return;
        }

        if (permissionService.hasPermission(authentication, "MOVES_CREATE_OWN")) {
            if (!residentRepository.existsActiveResidentForUserAndUnit(authentication.getName(), unitId)) {
                throw new AccessDeniedException("User can only create move requests for their own unit");
            }
            return;
        }

        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + unitId));
        UUID buildingId = unit.getBuilding().getId();
        if (!permissionService.hasPermission(authentication, "MOVES_CREATE", buildingId)) {
            throw new AccessDeniedException("User is not allowed to create move requests for this building");
        }
        // Non-administrator roles that receive MOVES_CREATE are expected to have
        // their building scope validated by MoveAccessService.canCreate().
    }

    private Resident resolveSelectedResidentForUnit(UUID residentId, Unit unit) {
        if (residentId == null) {
            return null;
        }

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found: " + residentId));

        if (!resident.isActive()
                || resident.getUnit() == null
                || !resident.getUnit().getId().equals(unit.getId())) {
            throw new IllegalArgumentException("Selected resident must be active and belong to the selected unit");
        }

        return resident;
    }

    private boolean requiresOwnerAuthorization(MoveRequest move) {
        return move.getResident() != null && move.getResident().getResidentType() != Enums.ResidentType.OWNER;
    }

    private void validateTransition(Enums.MoveRequestStatus current, Enums.MoveRequestStatus target) {
        boolean valid = switch (current) {
            case REQUESTED, PENDING_APPROVAL -> target == Enums.MoveRequestStatus.APPROVED
                    || target == Enums.MoveRequestStatus.REJECTED
                    || target == Enums.MoveRequestStatus.CANCELLED;
            case APPROVED -> target == Enums.MoveRequestStatus.SCHEDULED
                    || target == Enums.MoveRequestStatus.CANCELLED;
            case SCHEDULED -> target == Enums.MoveRequestStatus.COMPLETED
                    || target == Enums.MoveRequestStatus.CANCELLED;
            case REJECTED, COMPLETED, CANCELLED -> false;
        };
        if (!valid) throw new IllegalStateException("Invalid move status transition from " + current + " to " + target);
    }

    private void validateSchedule(OffsetDateTime start, OffsetDateTime end) {
        if ((start == null) != (end == null)) {
            throw new IllegalArgumentException("scheduledStart and scheduledEnd must be provided together");
        }
        if (start != null && !end.isAfter(start)) {
            throw new IllegalArgumentException("scheduledEnd must be after scheduledStart");
        }
    }

    private void validateActiveUnit(Unit unit) {
        if (!unit.isActive()) throw new IllegalStateException("Unit is inactive");
        if (unit.getBuilding() == null || !unit.getBuilding().isActive()) throw new IllegalStateException("Unit belongs to an inactive building");
    }

    private MoveRequest getEntity(UUID id) {
        return moveRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Move request not found: " + id));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private List<MoveUnitOptionResponse> toUnitOptions(List<Unit> units) {
        return units.stream()
                .filter(u -> u.getBuilding() != null && u.getBuilding().isActive() && u.isActive())
                .map(u -> new MoveUnitOptionResponse(u.getId(), u.getUnitNumber(), u.getBuilding().getId(), u.getBuilding().getCode()))
                .toList();
    }

    private MoveResponse toResponse(MoveRequest m) {
        Staff approved = m.getApprovedByStaff();
        User ownerAuth = m.getOwnerAuthorizedByUser();
        Resident resident = m.getResident();
        return new MoveResponse(
                m.getId(), m.getBuilding().getId(), m.getBuilding().getCode(), m.getUnit().getId(), m.getUnit().getUnitNumber(),
                resident == null ? null : resident.getId(),
                resident == null ? null : resident.getUser().getFirstName() + " " + resident.getUser().getLastName(),
                resident == null ? null : resident.getResidentType().name(),
                m.getRequestType().name(), m.getStatus().name(), m.getRequestedAt(), m.getScheduledStart(), m.getScheduledEnd(),
                approved == null ? null : approved.getId(),
                approved == null ? null : approved.getUser().getFirstName() + " " + approved.getUser().getLastName(),
                m.getApprovedAt(), m.isOwnerAuthorized(),
                ownerAuth == null ? null : ownerAuth.getId(),
                ownerAuth == null ? null : ownerAuth.getFirstName() + " " + ownerAuth.getLastName(),
                m.getOwnerAuthorizedAt(), m.getOwnerAuthorizationNotes(), m.getNotes(), m.getCreatedAt(), m.getUpdatedAt()
        );
    }

    private void audit(String actorEmail, MoveRequest move, String action, String previousStatus, String newStatus, Map<String, Object> details) {
        AuditLog log = new AuditLog();
        log.setBuilding(move.getBuilding());
        log.setEntityType("MOVE_REQUEST");
        log.setEntityId(move.getId());
        log.setAction(action);
        log.setActorUser(userRepository.findByEmailIgnoreCase(actorEmail).orElse(null));
        log.setPreviousStatus(previousStatus);
        log.setNewStatus(newStatus);
        log.setDetails(new LinkedHashMap<>(details));
        log.getDetails().put("requestType", move.getRequestType().name());
        log.getDetails().put("unitId", move.getUnit().getId().toString());
        log.setOccurredAt(OffsetDateTime.now());
        log.setUpdatedBy(move.getUpdatedBy());
        auditLogRepository.save(log);
    }

    private boolean hasRole(Authentication authentication, String roleCode) {
        return authentication.getAuthorities().stream().anyMatch(a -> ("ROLE_" + roleCode).equals(a.getAuthority()));
    }
}