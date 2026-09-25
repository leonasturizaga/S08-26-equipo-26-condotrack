//----------------------- M17.1 ----------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.IncidentAssignmentRequest;
// import com.condotrack.backend.dto.IncidentCreateRequest;
// import com.condotrack.backend.dto.IncidentPageResponse;
// import com.condotrack.backend.dto.IncidentResponse;
// import com.condotrack.backend.dto.IncidentUnitOptionResponse;
// import com.condotrack.backend.dto.IncidentStaffResponse;
// import com.condotrack.backend.dto.IncidentStatusUpdateRequest;
// import com.condotrack.backend.dto.IncidentUpdateRequest;
// import com.condotrack.backend.model.Building;
// import com.condotrack.backend.model.Incident;
// import com.condotrack.backend.model.Enums;
// import com.condotrack.backend.model.Staff;
// import com.condotrack.backend.model.Unit;
// import com.condotrack.backend.model.User;
// import com.condotrack.backend.repository.IncidentRepository;
// import com.condotrack.backend.repository.StaffRepository;
// import com.condotrack.backend.repository.UnitRepository;
// import com.condotrack.backend.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.OffsetDateTime;
// import java.util.List;
// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// public class IncidentService {

//     private static final int MAX_PAGE_SIZE = 100;

//     private final IncidentRepository incidentRepository;
//     private final UnitRepository unitRepository;
//     private final StaffRepository staffRepository;
//     private final UserRepository userRepository;
//     private final PermissionService permissionService;

//     @Transactional(readOnly = true)
//     public IncidentPageResponse getIncidents(Authentication authentication, int page, int size) {
//         Pageable pageable = createPageable(page, size);
//         Page<IncidentResponse> incidents;

//         if (permissionService.hasPermission(authentication, "INCIDENTS_VIEW")) {
//             List<UUID> staffBuildingIds = staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
//                     .map(staff -> staff.getBuilding().getId())
//                     .distinct()
//                     .filter(buildingId -> permissionService.hasPermission(authentication, "INCIDENTS_VIEW", buildingId))
//                     .toList();

//             boolean administrator = authentication.getAuthorities().stream()
//                     .anyMatch(authority -> "ROLE_ADMINISTRATOR".equals(authority.getAuthority()));

//             if (administrator || staffBuildingIds.isEmpty()) {
//                 incidents = incidentRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
//             } else {
//                 incidents = incidentRepository.findByBuilding_IdInOrderByCreatedAtDesc(staffBuildingIds, pageable).map(this::toResponse);
//             }
//         } else if (permissionService.hasPermission(authentication, "INCIDENTS_VIEW_ASSIGNED")) {
//             incidents = incidentRepository
//                     .findByAssignedToStaff_User_EmailIgnoreCaseOrderByCreatedAtDesc(
//                             authentication.getName(), pageable
//                     )
//                     .map(this::toResponse);
//         } else if (permissionService.hasPermission(authentication, "INCIDENTS_VIEW_OWN")) {
//             incidents = incidentRepository
//                     .findByReportedByUser_EmailIgnoreCaseOrderByCreatedAtDesc(
//                             authentication.getName(), pageable
//                     )
//                     .map(this::toResponse);
//         } else if (permissionService.hasPermission(authentication, "INCIDENTS_VIEW_UNIT")) {
//             incidents = incidentRepository
//                     .findForOwnedUnits(authentication.getName(), pageable)
//                     .map(this::toResponse);
//         } else {
//             throw new IllegalStateException("User is not allowed to view incidents");
//         }

//         return IncidentPageResponse.from(incidents);
//     }

//     @Transactional(readOnly = true)
//     public IncidentResponse getIncident(UUID incidentId) {
//         return toResponse(incidentRepository.findById(incidentId)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Incident not found: " + incidentId
//                 )));
//     }

//     @Transactional
//     public IncidentResponse createIncident(
//             IncidentCreateRequest request,
//             Authentication authentication
//     ) {
//         Unit unit = unitRepository.findById(request.unitId())
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Unit not found: " + request.unitId()
//                 ));

//         if (!unit.isActive()) {
//             throw new IllegalStateException("Unit is inactive");
//         }

//         Building building = unit.getBuilding();
//         if (building == null || !building.isActive()) {
//             throw new IllegalStateException("Unit belongs to an inactive building");
//         }

//         User reporter = getAuthenticatedUser(authentication);
//         Incident incident = new Incident();
//         incident.setBuilding(building);
//         incident.setUnit(unit);
//         incident.setReportedByUser(reporter);
//         incident.setStatus(Enums.IncidentStatus.CREATED);
//         incident.setSeverity(request.severity());
//         incident.setTitle(request.title().trim());
//         incident.setDescription(request.description().trim());
//         incident.setOccurredAt(request.occurredAt());
//         incident.setUpdatedBy(reporter.getId());

//         return toResponse(incidentRepository.save(incident));
//     }

//     @Transactional
//     public IncidentResponse updateIncident(
//             UUID incidentId,
//             IncidentUpdateRequest request,
//             Authentication authentication
//     ) {
//         Incident incident = getIncidentEntity(incidentId);
//         User currentUser = getAuthenticatedUser(authentication);

//         incident.setTitle(request.title().trim());
//         incident.setDescription(request.description().trim());
//         incident.setSeverity(request.severity());
//         incident.setOccurredAt(request.occurredAt());
//         incident.setUpdatedBy(currentUser.getId());

//         return toResponse(incidentRepository.save(incident));
//     }

//     @Transactional
//     public IncidentResponse assignIncident(
//             UUID incidentId,
//             IncidentAssignmentRequest request,
//             Authentication authentication
//     ) {
//         Incident incident = getIncidentEntity(incidentId);
//         User currentUser = getAuthenticatedUser(authentication);

//         if (request.assignedToStaffId() == null) {
//             incident.setAssignedToStaff(null);
//             if (incident.getStatus() == Enums.IncidentStatus.ASSIGNED) {
//                 incident.setStatus(Enums.IncidentStatus.CREATED);
//             }
//         } else {
//             Staff staff = staffRepository.findByIdAndActiveTrue(request.assignedToStaffId())
//                     .orElseThrow(() -> new IllegalArgumentException(
//                             "Active staff member not found: " + request.assignedToStaffId()
//                     ));

//             if (!staff.getBuilding().getId().equals(incident.getBuilding().getId())) {
//                 throw new IllegalArgumentException(
//                         "Assigned staff member must belong to the incident building"
//                 );
//             }

//             incident.setAssignedToStaff(staff);
//             if (incident.getStatus() == Enums.IncidentStatus.CREATED) {
//                 incident.setStatus(Enums.IncidentStatus.ASSIGNED);
//             }
//         }

//         incident.setUpdatedBy(currentUser.getId());
//         return toResponse(incidentRepository.save(incident));
//     }

//     @Transactional
//     public IncidentResponse updateStatus(
//             UUID incidentId,
//             IncidentStatusUpdateRequest request,
//             Authentication authentication
//     ) {
//         Incident incident = getIncidentEntity(incidentId);
//         User currentUser = getAuthenticatedUser(authentication);

//         Enums.IncidentStatus currentStatus = incident.getStatus();
//         Enums.IncidentStatus targetStatus = request.status();
//         boolean administrator = permissionService.hasPermission(authentication, "INCIDENTS_UPDATE", incident.getBuilding().getId());

//         validateTransition(currentStatus, targetStatus, administrator);

//         if (!administrator && incident.getAssignedToStaff() == null) {
//             throw new IllegalStateException("Incident is not assigned to the authenticated provider");
//         }

//         if (!administrator && targetStatus == Enums.IncidentStatus.ASSIGNED) {
//             throw new IllegalStateException("Providers cannot assign incidents");
//         }

//         if (targetStatus == Enums.IncidentStatus.ASSIGNED && incident.getAssignedToStaff() == null) {
//             throw new IllegalStateException("An incident must be assigned before entering ASSIGNED status");
//         }

//         if (!administrator
//                 && targetStatus != Enums.IncidentStatus.IN_PROGRESS
//                 && targetStatus != Enums.IncidentStatus.RESOLVED
//                 && targetStatus != Enums.IncidentStatus.CLOSED) {
//             throw new IllegalStateException("Providers can only move assigned incidents through IN_PROGRESS, RESOLVED and CLOSED");
//         }

//         if (targetStatus == Enums.IncidentStatus.RESOLVED
//                 && (request.resolution() == null || request.resolution().trim().isEmpty())) {
//             throw new IllegalArgumentException("A resolution is required when resolving an incident");
//         }

//         incident.setStatus(targetStatus);
//         incident.setUpdatedBy(currentUser.getId());

//         if (request.resolution() != null && !request.resolution().trim().isEmpty()) {
//             incident.setResolution(request.resolution().trim());
//         }

//         if (targetStatus == Enums.IncidentStatus.RESOLVED) {
//             incident.setResolvedAt(OffsetDateTime.now());
//         } else if (currentStatus == Enums.IncidentStatus.RESOLVED
//                 && targetStatus == Enums.IncidentStatus.IN_PROGRESS) {
//             incident.setResolvedAt(null);
//         }

//         return toResponse(incidentRepository.save(incident));
//     }


//     @Transactional(readOnly = true)
//     public List<IncidentUnitOptionResponse> getIncidentUnitOptions(Authentication authentication) {
//         if (permissionService.hasPermission(authentication, "INCIDENTS_CREATE")) {
//             return unitRepository.findByActiveTrueOrderByBuildingIdAscUnitNumberAsc()
//                     .stream()
//                     .filter(unit -> unit.getBuilding() != null
//                             && unit.getBuilding().isActive()
//                             && permissionService.hasPermission(authentication, "INCIDENTS_CREATE", unit.getBuilding().getId()))
//                     .map(unit -> new IncidentUnitOptionResponse(
//                             unit.getId(),
//                             unit.getUnitNumber(),
//                             unit.getBuilding().getId(),
//                             unit.getBuilding().getCode()
//                     ))
//                     .toList();
//         }

//         if (permissionService.hasPermission(authentication, "INCIDENTS_CREATE_OWN")) {
//             return unitRepository.findActiveUnitsForUser(
//                             authentication.getName(),
//                             PageRequest.of(0, MAX_PAGE_SIZE)
//                     )
//                     .getContent()
//                     .stream()
//                     .filter(unit -> unit.getBuilding() != null && unit.getBuilding().isActive())
//                     .map(unit -> new IncidentUnitOptionResponse(
//                             unit.getId(),
//                             unit.getUnitNumber(),
//                             unit.getBuilding().getId(),
//                             unit.getBuilding().getCode()
//                     ))
//                     .toList();
//         }

//         throw new org.springframework.security.access.AccessDeniedException(
//                 "User is not allowed to create incidents"
//         );
//     }

//     @Transactional(readOnly = true)
//     public List<IncidentStaffResponse> getAssignableStaff(UUID buildingId) {
//         return staffRepository.findByBuildingIdAndActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc(buildingId)
//                 .stream()
//                 .map(staff -> new IncidentStaffResponse(
//                         staff.getId(),
//                         staff.getUser().getId(),
//                         staff.getBuilding().getId(),
//                         staff.getUser().getFirstName(),
//                         staff.getUser().getLastName(),
//                         staff.getStaffType().name(),
//                         staff.getEmployeeCode()
//                 ))
//                 .toList();
//     }

//     private Incident getIncidentEntity(UUID incidentId) {
//         return incidentRepository.findById(incidentId)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Incident not found: " + incidentId
//                 ));
//     }

//     private User getAuthenticatedUser(Authentication authentication) {
//         if (authentication == null || authentication.getName() == null) {
//             throw new IllegalStateException("Authenticated user is required");
//         }

//         return userRepository.findByEmailIgnoreCase(authentication.getName())
//                 .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
//     }

//     private Pageable createPageable(int page, int size) {
//         int safePage = Math.max(page, 0);
//         int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
//         return PageRequest.of(safePage, safeSize);
//     }

//     private void validateTransition(
//             Enums.IncidentStatus currentStatus,
//             Enums.IncidentStatus targetStatus,
//             boolean administrator
//     ) {
//         if (currentStatus == targetStatus) {
//             throw new IllegalStateException("Incident is already in status " + targetStatus);
//         }

//         if (targetStatus == Enums.IncidentStatus.ASSIGNED && !administrator) {
//             throw new IllegalStateException("Only administrators can assign incidents");
//         }

//         boolean valid = switch (currentStatus) {
//             case CREATED -> targetStatus == Enums.IncidentStatus.ASSIGNED
//                     || targetStatus == Enums.IncidentStatus.CANCELLED;
//             case ASSIGNED -> targetStatus == Enums.IncidentStatus.IN_PROGRESS
//                     || targetStatus == Enums.IncidentStatus.CANCELLED;
//             case IN_PROGRESS -> targetStatus == Enums.IncidentStatus.RESOLVED
//                     || targetStatus == Enums.IncidentStatus.CANCELLED;
//             case RESOLVED -> targetStatus == Enums.IncidentStatus.CLOSED
//                     || targetStatus == Enums.IncidentStatus.IN_PROGRESS;
//             case CLOSED, CANCELLED -> false;
//         };

//         if (!valid) {
//             throw new IllegalStateException(
//                     "Invalid incident status transition from "
//                             + currentStatus + " to " + targetStatus
//             );
//         }
//     }

//     private IncidentResponse toResponse(Incident incident) {
//         String reportedByName = incident.getReportedByUser() == null
//                 ? null
//                 : incident.getReportedByUser().getFirstName() + " "
//                 + incident.getReportedByUser().getLastName();

//         Staff assigned = incident.getAssignedToStaff();
//         String assignedName = assigned == null || assigned.getUser() == null
//                 ? null
//                 : assigned.getUser().getFirstName() + " "
//                 + assigned.getUser().getLastName();

//         return new IncidentResponse(
//                 incident.getId(),
//                 incident.getBuilding().getId(),
//                 incident.getBuilding().getCode(),
//                 incident.getUnit().getId(),
//                 incident.getUnit().getUnitNumber(),
//                 incident.getReportedByUser().getId(),
//                 reportedByName,
//                 assigned == null ? null : assigned.getId(),
//                 assignedName,
//                 assigned == null || assigned.getStaffType() == null
//                         ? null
//                         : assigned.getStaffType().name(),
//                 incident.getStatus().name(),
//                 incident.getSeverity().name(),
//                 incident.getTitle(),
//                 incident.getDescription(),
//                 incident.getOccurredAt(),
//                 incident.getResolvedAt(),
//                 incident.getResolution(),
//                 incident.getCreatedAt(),
//                 incident.getUpdatedAt()
//         );
//     }
// }


//----------------------- milestone 20 ----------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.IncidentAssignmentRequest;
import com.condotrack.backend.dto.IncidentCreateRequest;
import com.condotrack.backend.dto.IncidentPageResponse;
import com.condotrack.backend.dto.IncidentResponse;
import com.condotrack.backend.dto.IncidentUnitOptionResponse;
import com.condotrack.backend.dto.IncidentStaffResponse;
import com.condotrack.backend.dto.IncidentStatusUpdateRequest;
import com.condotrack.backend.dto.IncidentUpdateRequest;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.Incident;
import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.Staff;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.IncidentRepository;
import com.condotrack.backend.repository.StaffRepository;
import com.condotrack.backend.repository.UnitRepository;
import com.condotrack.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private static final int MAX_PAGE_SIZE = 100;

    private final IncidentRepository incidentRepository;
    private final UnitRepository unitRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public IncidentPageResponse getIncidents(Authentication authentication, int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<IncidentResponse> incidents;

        if (permissionService.hasPermission(authentication, "INCIDENTS_VIEW")) {
            List<UUID> staffBuildingIds = staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
                    .map(staff -> staff.getBuilding().getId())
                    .distinct()
                    .filter(buildingId -> permissionService.hasPermission(authentication, "INCIDENTS_VIEW", buildingId))
                    .toList();

            boolean administrator = authentication.getAuthorities().stream()
                    .anyMatch(authority -> "ROLE_ADMINISTRATOR".equals(authority.getAuthority()));

            if (administrator || staffBuildingIds.isEmpty()) {
                incidents = incidentRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
            } else {
                incidents = incidentRepository.findByBuilding_IdInOrderByCreatedAtDesc(staffBuildingIds, pageable).map(this::toResponse);
            }
        } else if (permissionService.hasPermission(authentication, "INCIDENTS_VIEW_ASSIGNED")) {
            incidents = incidentRepository
                    .findByAssignedToStaff_User_EmailIgnoreCaseOrderByCreatedAtDesc(
                            authentication.getName(), pageable
                    )
                    .map(this::toResponse);
        } else if (permissionService.hasPermission(authentication, "INCIDENTS_VIEW_OWN")) {
            incidents = incidentRepository
                    .findByReportedByUser_EmailIgnoreCaseOrderByCreatedAtDesc(
                            authentication.getName(), pageable
                    )
                    .map(this::toResponse);
        } else if (permissionService.hasPermission(authentication, "INCIDENTS_VIEW_UNIT")) {
            incidents = incidentRepository
                    .findForOwnedUnits(authentication.getName(), pageable)
                    .map(this::toResponse);
        } else {
            throw new IllegalStateException("User is not allowed to view incidents");
        }

        return IncidentPageResponse.from(incidents);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncident(UUID incidentId) {
        return toResponse(incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Incident not found: " + incidentId
                )));
    }

    @Transactional
    public IncidentResponse createIncident(
            IncidentCreateRequest request,
            Authentication authentication
    ) {
        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unit not found: " + request.unitId()
                ));

        if (!unit.isActive()) {
            throw new IllegalStateException("Unit is inactive");
        }

        Building building = unit.getBuilding();
        if (building == null || !building.isActive()) {
            throw new IllegalStateException("Unit belongs to an inactive building");
        }

        User reporter = getAuthenticatedUser(authentication);
        Incident incident = new Incident();
        incident.setBuilding(building);
        incident.setUnit(unit);
        incident.setReportedByUser(reporter);
        incident.setStatus(Enums.IncidentStatus.CREATED);
        incident.setSeverity(request.severity());
        incident.setTitle(request.title().trim());
        incident.setDescription(request.description().trim());
        incident.setOccurredAt(request.occurredAt());
        incident.setUpdatedBy(reporter.getId());

        Incident saved = incidentRepository.save(incident);
        auditService.record(authentication.getName(), building, "INCIDENT", saved.getId(), "CREATE", null, saved.getStatus().name(), null, java.util.Map.of("unitId", unit.getId().toString()));
        return toResponse(saved);
    }

    @Transactional
    public IncidentResponse updateIncident(
            UUID incidentId,
            IncidentUpdateRequest request,
            Authentication authentication
    ) {
        Incident incident = getIncidentEntity(incidentId);
        User currentUser = getAuthenticatedUser(authentication);

        incident.setTitle(request.title().trim());
        incident.setDescription(request.description().trim());
        incident.setSeverity(request.severity());
        incident.setOccurredAt(request.occurredAt());
        incident.setUpdatedBy(currentUser.getId());

        Incident saved = incidentRepository.save(incident);
        auditService.record(authentication.getName(), incident.getBuilding(), "INCIDENT", saved.getId(), "UPDATE", java.util.Map.of("severity", saved.getSeverity().name()));
        return toResponse(saved);
    }

    @Transactional
    public IncidentResponse assignIncident(
            UUID incidentId,
            IncidentAssignmentRequest request,
            Authentication authentication
    ) {
        Incident incident = getIncidentEntity(incidentId);
        User currentUser = getAuthenticatedUser(authentication);

        if (request.assignedToStaffId() == null) {
            incident.setAssignedToStaff(null);
            if (incident.getStatus() == Enums.IncidentStatus.ASSIGNED) {
                incident.setStatus(Enums.IncidentStatus.CREATED);
            }
        } else {
            Staff staff = staffRepository.findByIdAndActiveTrue(request.assignedToStaffId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Active staff member not found: " + request.assignedToStaffId()
                    ));

            if (!staff.getBuilding().getId().equals(incident.getBuilding().getId())) {
                throw new IllegalArgumentException(
                        "Assigned staff member must belong to the incident building"
                );
            }

            incident.setAssignedToStaff(staff);
            if (incident.getStatus() == Enums.IncidentStatus.CREATED) {
                incident.setStatus(Enums.IncidentStatus.ASSIGNED);
            }
        }

        incident.setUpdatedBy(currentUser.getId());
        Incident saved = incidentRepository.save(incident);
        auditService.record(authentication.getName(), saved.getBuilding(), "INCIDENT", saved.getId(), "ASSIGNED", saved.getStatus().name(), saved.getStatus().name(), null, saved.getAssignedToStaff() == null ? java.util.Map.of() : java.util.Map.of("staffId", saved.getAssignedToStaff().getId().toString()));
        return toResponse(saved);
    }

    @Transactional
    public IncidentResponse updateStatus(
            UUID incidentId,
            IncidentStatusUpdateRequest request,
            Authentication authentication
    ) {
        Incident incident = getIncidentEntity(incidentId);
        User currentUser = getAuthenticatedUser(authentication);

        Enums.IncidentStatus currentStatus = incident.getStatus();
        Enums.IncidentStatus targetStatus = request.status();
        boolean administrator = permissionService.hasPermission(authentication, "INCIDENTS_UPDATE", incident.getBuilding().getId());

        validateTransition(currentStatus, targetStatus, administrator);

        if (!administrator && incident.getAssignedToStaff() == null) {
            throw new IllegalStateException("Incident is not assigned to the authenticated provider");
        }

        if (!administrator && targetStatus == Enums.IncidentStatus.ASSIGNED) {
            throw new IllegalStateException("Providers cannot assign incidents");
        }

        if (targetStatus == Enums.IncidentStatus.ASSIGNED && incident.getAssignedToStaff() == null) {
            throw new IllegalStateException("An incident must be assigned before entering ASSIGNED status");
        }

        if (!administrator
                && targetStatus != Enums.IncidentStatus.IN_PROGRESS
                && targetStatus != Enums.IncidentStatus.RESOLVED
                && targetStatus != Enums.IncidentStatus.CLOSED) {
            throw new IllegalStateException("Providers can only move assigned incidents through IN_PROGRESS, RESOLVED and CLOSED");
        }

        if (targetStatus == Enums.IncidentStatus.RESOLVED
                && (request.resolution() == null || request.resolution().trim().isEmpty())) {
            throw new IllegalArgumentException("A resolution is required when resolving an incident");
        }

        incident.setStatus(targetStatus);
        incident.setUpdatedBy(currentUser.getId());

        if (request.resolution() != null && !request.resolution().trim().isEmpty()) {
            incident.setResolution(request.resolution().trim());
        }

        if (targetStatus == Enums.IncidentStatus.RESOLVED) {
            incident.setResolvedAt(OffsetDateTime.now());
        } else if (currentStatus == Enums.IncidentStatus.RESOLVED
                && targetStatus == Enums.IncidentStatus.IN_PROGRESS) {
            incident.setResolvedAt(null);
        }

        Incident saved = incidentRepository.save(incident);
        auditService.record(authentication.getName(), saved.getBuilding(), "INCIDENT", saved.getId(), "STATUS_CHANGED", currentStatus.name(), saved.getStatus().name(), saved.getResolution(), java.util.Map.of());
        return toResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<IncidentUnitOptionResponse> getIncidentUnitOptions(Authentication authentication) {
        if (permissionService.hasPermission(authentication, "INCIDENTS_CREATE")) {
            return unitRepository.findByActiveTrueOrderByBuildingIdAscUnitNumberAsc()
                    .stream()
                    .filter(unit -> unit.getBuilding() != null
                            && unit.getBuilding().isActive()
                            && permissionService.hasPermission(authentication, "INCIDENTS_CREATE", unit.getBuilding().getId()))
                    .map(unit -> new IncidentUnitOptionResponse(
                            unit.getId(),
                            unit.getUnitNumber(),
                            unit.getBuilding().getId(),
                            unit.getBuilding().getCode()
                    ))
                    .toList();
        }

        if (permissionService.hasPermission(authentication, "INCIDENTS_CREATE_OWN")) {
            return unitRepository.findActiveUnitsForUser(
                            authentication.getName(),
                            PageRequest.of(0, MAX_PAGE_SIZE)
                    )
                    .getContent()
                    .stream()
                    .filter(unit -> unit.getBuilding() != null && unit.getBuilding().isActive())
                    .map(unit -> new IncidentUnitOptionResponse(
                            unit.getId(),
                            unit.getUnitNumber(),
                            unit.getBuilding().getId(),
                            unit.getBuilding().getCode()
                    ))
                    .toList();
        }

        throw new org.springframework.security.access.AccessDeniedException(
                "User is not allowed to create incidents"
        );
    }

    @Transactional(readOnly = true)
    public List<IncidentStaffResponse> getAssignableStaff(UUID buildingId) {
        return staffRepository.findByBuildingIdAndActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc(buildingId)
                .stream()
                .map(staff -> new IncidentStaffResponse(
                        staff.getId(),
                        staff.getUser().getId(),
                        staff.getBuilding().getId(),
                        staff.getUser().getFirstName(),
                        staff.getUser().getLastName(),
                        staff.getStaffType().name(),
                        staff.getEmployeeCode()
                ))
                .toList();
    }

    private Incident getIncidentEntity(UUID incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Incident not found: " + incidentId
                ));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user is required");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private Pageable createPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize);
    }

    private void validateTransition(
            Enums.IncidentStatus currentStatus,
            Enums.IncidentStatus targetStatus,
            boolean administrator
    ) {
        if (currentStatus == targetStatus) {
            throw new IllegalStateException("Incident is already in status " + targetStatus);
        }

        if (targetStatus == Enums.IncidentStatus.ASSIGNED && !administrator) {
            throw new IllegalStateException("Only administrators can assign incidents");
        }

        boolean valid = switch (currentStatus) {
            case CREATED -> targetStatus == Enums.IncidentStatus.ASSIGNED
                    || targetStatus == Enums.IncidentStatus.CANCELLED;
            case ASSIGNED -> targetStatus == Enums.IncidentStatus.IN_PROGRESS
                    || targetStatus == Enums.IncidentStatus.CANCELLED;
            case IN_PROGRESS -> targetStatus == Enums.IncidentStatus.RESOLVED
                    || targetStatus == Enums.IncidentStatus.CANCELLED;
            case RESOLVED -> targetStatus == Enums.IncidentStatus.CLOSED
                    || targetStatus == Enums.IncidentStatus.IN_PROGRESS;
            case CLOSED, CANCELLED -> false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    "Invalid incident status transition from "
                            + currentStatus + " to " + targetStatus
            );
        }
    }

    private IncidentResponse toResponse(Incident incident) {
        String reportedByName = incident.getReportedByUser() == null
                ? null
                : incident.getReportedByUser().getFirstName() + " "
                + incident.getReportedByUser().getLastName();

        Staff assigned = incident.getAssignedToStaff();
        String assignedName = assigned == null || assigned.getUser() == null
                ? null
                : assigned.getUser().getFirstName() + " "
                + assigned.getUser().getLastName();

        return new IncidentResponse(
                incident.getId(),
                incident.getBuilding().getId(),
                incident.getBuilding().getCode(),
                incident.getUnit().getId(),
                incident.getUnit().getUnitNumber(),
                incident.getReportedByUser().getId(),
                reportedByName,
                assigned == null ? null : assigned.getId(),
                assignedName,
                assigned == null || assigned.getStaffType() == null
                        ? null
                        : assigned.getStaffType().name(),
                incident.getStatus().name(),
                incident.getSeverity().name(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getOccurredAt(),
                incident.getResolvedAt(),
                incident.getResolution(),
                incident.getCreatedAt(),
                incident.getUpdatedAt()
        );
    }
}
