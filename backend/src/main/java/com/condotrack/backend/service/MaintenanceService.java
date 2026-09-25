//----------------------- M17.1 ----------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.*;
// import com.condotrack.backend.model.Building;
// import com.condotrack.backend.model.Enums;
// import com.condotrack.backend.model.Incident;
// import com.condotrack.backend.model.MaintenanceRequest;
// import com.condotrack.backend.model.Staff;
// import com.condotrack.backend.model.Unit;
// import com.condotrack.backend.model.User;
// import com.condotrack.backend.repository.IncidentRepository;
// import com.condotrack.backend.repository.MaintenanceRequestRepository;
// import com.condotrack.backend.repository.ResidentRepository;
// import com.condotrack.backend.repository.StaffRepository;
// import com.condotrack.backend.repository.UnitRepository;
// import com.condotrack.backend.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.security.access.AccessDeniedException;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.OffsetDateTime;
// import java.util.List;
// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// public class MaintenanceService {
//     private static final int MAX_PAGE_SIZE = 100;

//     private final MaintenanceRequestRepository maintenanceRepository;
//     private final UnitRepository unitRepository;
//     private final StaffRepository staffRepository;
//     private final UserRepository userRepository;
//     private final ResidentRepository residentRepository;
//     private final IncidentRepository incidentRepository;
//     private final PermissionService permissionService;

//     @Transactional(readOnly = true)
//     public MaintenancePageResponse getMaintenance(Authentication authentication, int page, int size) {
//         Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
//         Page<MaintenanceResponse> result;
//         if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW")) {
//             List<UUID> staffBuildingIds = staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
//                     .map(staff -> staff.getBuilding().getId())
//                     .distinct()
//                     .filter(buildingId -> permissionService.hasPermission(authentication, "MAINTENANCE_VIEW", buildingId))
//                     .toList();

//             boolean administrator = authentication.getAuthorities().stream()
//                     .anyMatch(authority -> "ROLE_ADMINISTRATOR".equals(authority.getAuthority()));

//             if (administrator || staffBuildingIds.isEmpty()) {
//                 result = maintenanceRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
//             } else {
//                 result = maintenanceRepository.findByBuilding_IdInOrderByCreatedAtDesc(staffBuildingIds, pageable).map(this::toResponse);
//             }
//         } else if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_ASSIGNED")) {
//             result = maintenanceRepository.findByAssignedToStaff_User_EmailIgnoreCaseOrderByCreatedAtDesc(authentication.getName(), pageable).map(this::toResponse);
//         } else if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_OWN")) {
//             result = maintenanceRepository.findByRequestedByUser_EmailIgnoreCaseOrderByCreatedAtDesc(authentication.getName(), pageable).map(this::toResponse);
//         } else if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_UNIT")) {
//             result = maintenanceRepository.findForOwnedUnits(authentication.getName(), pageable).map(this::toResponse);
//         } else {
//             throw new AccessDeniedException("User is not allowed to view maintenance");
//         }
//         return MaintenancePageResponse.from(result);
//     }

//     @Transactional(readOnly = true)
//     public MaintenanceResponse getMaintenance(UUID id) {
//         return toResponse(getEntity(id));
//     }

//     @Transactional
//     public MaintenanceResponse createMaintenance(MaintenanceCreateRequest request, Authentication authentication) {
//         Unit unit = unitRepository.findById(request.unitId())
//                 .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + request.unitId()));
//         validateUnitActive(unit);
//         validateCreateScope(authentication, unit);

//         User requester = getAuthenticatedUser(authentication);
//         MaintenanceRequest maintenance = new MaintenanceRequest();
//         maintenance.setBuilding(unit.getBuilding());
//         maintenance.setUnit(unit);
//         maintenance.setRequestedByUser(requester);
//         maintenance.setStatus(Enums.MaintenanceStatus.CREATED);
//         maintenance.setPriority(request.priority());
//         maintenance.setCategory(request.category().trim());
//         maintenance.setDescription(request.description().trim());
//         maintenance.setScheduledAt(request.scheduledAt());
//         maintenance.setUpdatedBy(requester.getId());

//         if (request.incidentId() != null) {
//             Incident incident = incidentRepository.findById(request.incidentId())
//                     .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + request.incidentId()));
//             if (!incident.getUnit().getId().equals(unit.getId())) {
//                 throw new IllegalArgumentException("Maintenance incident must belong to the selected unit");
//             }
//             maintenance.setIncident(incident);
//         }
//         return toResponse(maintenanceRepository.save(maintenance));
//     }

//     @Transactional
//     public MaintenanceResponse updateMaintenance(UUID id, MaintenanceUpdateRequest request, Authentication authentication) {
//         MaintenanceRequest maintenance = getEntity(id);
//         User currentUser = getAuthenticatedUser(authentication);
//         maintenance.setPriority(request.priority());
//         maintenance.setCategory(request.category().trim());
//         maintenance.setDescription(request.description().trim());
//         maintenance.setScheduledAt(request.scheduledAt());
//         maintenance.setUpdatedBy(currentUser.getId());
//         return toResponse(maintenanceRepository.save(maintenance));
//     }

//     @Transactional
//     public MaintenanceResponse assignMaintenance(UUID id, MaintenanceAssignmentRequest request, Authentication authentication) {
//         MaintenanceRequest maintenance = getEntity(id);
//         User currentUser = getAuthenticatedUser(authentication);
//         if (request.assignedToStaffId() == null) {
//             maintenance.setAssignedToStaff(null);
//             if (maintenance.getStatus() == Enums.MaintenanceStatus.ASSIGNED) maintenance.setStatus(Enums.MaintenanceStatus.CREATED);
//         } else {
//             Staff staff = staffRepository.findByIdAndActiveTrue(request.assignedToStaffId())
//                     .orElseThrow(() -> new IllegalArgumentException("Active maintenance staff member not found: " + request.assignedToStaffId()));
//             if (!staff.getBuilding().getId().equals(maintenance.getBuilding().getId())) {
//                 throw new IllegalArgumentException("Assigned staff member must belong to the maintenance building");
//             }
//             if (staff.getStaffType() != Enums.StaffType.MAINTENANCE) {
//                 throw new IllegalArgumentException("Assigned staff member must have MAINTENANCE staff type");
//             }
//             maintenance.setAssignedToStaff(staff);
//             if (maintenance.getStatus() == Enums.MaintenanceStatus.CREATED) maintenance.setStatus(Enums.MaintenanceStatus.ASSIGNED);
//         }
//         maintenance.setUpdatedBy(currentUser.getId());
//         return toResponse(maintenanceRepository.save(maintenance));
//     }

//     @Transactional
//     public MaintenanceResponse updateStatus(UUID id, MaintenanceStatusUpdateRequest request, Authentication authentication) {
//         MaintenanceRequest maintenance = getEntity(id);
//         User currentUser = getAuthenticatedUser(authentication);
//         Enums.MaintenanceStatus current = maintenance.getStatus();
//         Enums.MaintenanceStatus target = request.status();
//         boolean administrator = permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE", maintenance.getBuilding().getId());

//         if (current == target) throw new IllegalStateException("Maintenance request is already in status " + target);
//         if (!administrator && maintenance.getAssignedToStaff() == null) throw new IllegalStateException("Maintenance request is not assigned");
//         if (!administrator && target != Enums.MaintenanceStatus.IN_PROGRESS
//                 && target != Enums.MaintenanceStatus.ON_HOLD
//                 && target != Enums.MaintenanceStatus.RESOLVED
//                 && target != Enums.MaintenanceStatus.CLOSED) {
//             throw new IllegalStateException("Providers can only move assigned maintenance through IN_PROGRESS, ON_HOLD, RESOLVED and CLOSED");
//         }
//         validateTransition(current, target, administrator);
//         if (target == Enums.MaintenanceStatus.RESOLVED && (request.resolution() == null || request.resolution().trim().isEmpty())) {
//             throw new IllegalArgumentException("A resolution is required when resolving maintenance");
//         }
//         maintenance.setStatus(target);
//         maintenance.setUpdatedBy(currentUser.getId());
//         if (request.resolution() != null && !request.resolution().trim().isEmpty()) maintenance.setResolution(request.resolution().trim());
//         if (target == Enums.MaintenanceStatus.RESOLVED || target == Enums.MaintenanceStatus.CLOSED) {
//             if (maintenance.getCompletedAt() == null) maintenance.setCompletedAt(OffsetDateTime.now());
//         } else if (current == Enums.MaintenanceStatus.RESOLVED && target == Enums.MaintenanceStatus.IN_PROGRESS) {
//             maintenance.setCompletedAt(null);
//         }
//         return toResponse(maintenanceRepository.save(maintenance));
//     }

//     @Transactional(readOnly = true)
//     public List<MaintenanceUnitOptionResponse> getUnitOptions(Authentication authentication) {
//         if (permissionService.hasPermission(authentication, "MAINTENANCE_ASSIGN")) {
//             return toUnitOptions(unitRepository.findByActiveTrueOrderByBuildingIdAscUnitNumberAsc());
//         }
//         if (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE")) {
//             List<UUID> buildingIds = staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
//                     .map(staff -> staff.getBuilding().getId())
//                     .distinct()
//                     .filter(buildingId -> permissionService.hasPermission(authentication, "MAINTENANCE_CREATE", buildingId))
//                     .toList();
//             if (buildingIds.isEmpty() && authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMINISTRATOR".equals(a.getAuthority()))) {
//                 return toUnitOptions(unitRepository.findByActiveTrueOrderByBuildingIdAscUnitNumberAsc());
//             }
//             if (buildingIds.isEmpty()) return List.of();
//             return toUnitOptions(unitRepository.findByBuildingIdInAndActiveTrueOrderByBuildingIdAscUnitNumberAsc(buildingIds));
//         }
//         if (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE_OWN")) {
//             return toUnitOptions(unitRepository.findActiveUnitsForUser(authentication.getName(), PageRequest.of(0, MAX_PAGE_SIZE)).getContent());
//         }
//         throw new AccessDeniedException("User is not allowed to create maintenance");
//     }

//     @Transactional(readOnly = true)
//     public List<MaintenanceStaffResponse> getAssignableStaff(UUID buildingId) {
//         return staffRepository.findByBuildingIdAndActiveTrueAndStaffTypeOrderByUser_LastNameAscUser_FirstNameAsc(buildingId, Enums.StaffType.MAINTENANCE)
//                 .stream().map(staff -> new MaintenanceStaffResponse(
//                         staff.getId(), staff.getUser().getId(), staff.getBuilding().getId(),
//                         staff.getUser().getFirstName(), staff.getUser().getLastName(), staff.getStaffType().name(), staff.getEmployeeCode()))
//                 .toList();
//     }

//     private void validateCreateScope(Authentication authentication, Unit unit) {
//         UUID buildingId = unit.getBuilding().getId();
//         if (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE_OWN", buildingId)) {
//             if (!residentRepository.existsActiveResidentForUserAndUnit(authentication.getName(), unit.getId())) {
//                 throw new AccessDeniedException("User can only create maintenance for their own unit");
//             }
//             return;
//         }
//         if (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE", buildingId)
//                 || permissionService.hasPermission(authentication, "MAINTENANCE_ASSIGN", buildingId)) {
//             if (hasRole(authentication, "ADMINISTRATOR")) return;
//             if (!staffRepository.existsActiveByUserEmailAndBuildingId(authentication.getName(), buildingId)) {
//                 throw new AccessDeniedException("Reception user is not assigned to the selected building");
//             }
//             return;
//         }
//         throw new AccessDeniedException("User is not allowed to create maintenance for the selected building");
//     }

//     private boolean hasRole(Authentication authentication, String roleCode) {
//         return authentication.getAuthorities().stream()
//                 .anyMatch(authority -> ("ROLE_" + roleCode).equals(authority.getAuthority()));
//     }
//     private void validateUnitActive(Unit unit) {
//         if (!unit.isActive()) throw new IllegalStateException("Unit is inactive");
//         if (unit.getBuilding() == null || !unit.getBuilding().isActive()) throw new IllegalStateException("Unit belongs to an inactive building");
//     }

//     private void validateTransition(Enums.MaintenanceStatus current, Enums.MaintenanceStatus target, boolean administrator) {
//         if (target == Enums.MaintenanceStatus.ASSIGNED && !administrator) throw new IllegalStateException("Only administrators can assign maintenance");
//         boolean valid = switch (current) {
//             case CREATED -> target == Enums.MaintenanceStatus.ASSIGNED || target == Enums.MaintenanceStatus.CANCELLED;
//             case ASSIGNED -> target == Enums.MaintenanceStatus.IN_PROGRESS || target == Enums.MaintenanceStatus.CANCELLED;
//             case IN_PROGRESS -> target == Enums.MaintenanceStatus.ON_HOLD || target == Enums.MaintenanceStatus.RESOLVED || target == Enums.MaintenanceStatus.CANCELLED;
//             case ON_HOLD -> target == Enums.MaintenanceStatus.IN_PROGRESS || target == Enums.MaintenanceStatus.CANCELLED;
//             case RESOLVED -> target == Enums.MaintenanceStatus.CLOSED || target == Enums.MaintenanceStatus.IN_PROGRESS;
//             case CLOSED, CANCELLED -> false;
//         };
//         if (!valid) throw new IllegalStateException("Invalid maintenance status transition from " + current + " to " + target);
//     }

//     private MaintenanceRequest getEntity(UUID id) { return maintenanceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Maintenance request not found: " + id)); }
//     private User getAuthenticatedUser(Authentication authentication) { return userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow(() -> new IllegalStateException("Authenticated user not found")); }
//     private List<MaintenanceUnitOptionResponse> toUnitOptions(List<Unit> units) { return units.stream().filter(u -> u.getBuilding()!=null && u.getBuilding().isActive()).map(u -> new MaintenanceUnitOptionResponse(u.getId(), u.getUnitNumber(), u.getBuilding().getId(), u.getBuilding().getCode())).toList(); }
//     private MaintenanceResponse toResponse(MaintenanceRequest m) {
//         Staff staff = m.getAssignedToStaff();
//         User requester = m.getRequestedByUser();
//         return new MaintenanceResponse(
//                 m.getId(), m.getBuilding().getId(), m.getBuilding().getCode(), m.getUnit().getId(), m.getUnit().getUnitNumber(),
//                 m.getIncident() == null ? null : m.getIncident().getId(), requester == null ? null : requester.getId(),
//                 requester == null ? null : requester.getFirstName() + " " + requester.getLastName(),
//                 staff == null ? null : staff.getId(), staff == null ? null : staff.getUser().getFirstName() + " " + staff.getUser().getLastName(),
//                 staff == null || staff.getStaffType() == null ? null : staff.getStaffType().name(), staff == null ? null : staff.getEmployeeCode(),
//                 m.getStatus().name(), m.getPriority().name(), m.getCategory(), m.getDescription(), m.getScheduledAt(), m.getCompletedAt(), m.getResolution(), m.getCreatedAt(), m.getUpdatedAt());
//     }
// }


//----------------------- Milestone 20 ----------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.*;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.Incident;
import com.condotrack.backend.model.MaintenanceRequest;
import com.condotrack.backend.model.Staff;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.IncidentRepository;
import com.condotrack.backend.repository.MaintenanceRequestRepository;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaintenanceService {
    private static final int MAX_PAGE_SIZE = 100;

    private final MaintenanceRequestRepository maintenanceRepository;
    private final UnitRepository unitRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final IncidentRepository incidentRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public MaintenancePageResponse getMaintenance(Authentication authentication, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        Page<MaintenanceResponse> result;
        if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW")) {
            List<UUID> staffBuildingIds = staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
                    .map(staff -> staff.getBuilding().getId())
                    .distinct()
                    .filter(buildingId -> permissionService.hasPermission(authentication, "MAINTENANCE_VIEW", buildingId))
                    .toList();

            boolean administrator = authentication.getAuthorities().stream()
                    .anyMatch(authority -> "ROLE_ADMINISTRATOR".equals(authority.getAuthority()));

            if (administrator || staffBuildingIds.isEmpty()) {
                result = maintenanceRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
            } else {
                result = maintenanceRepository.findByBuilding_IdInOrderByCreatedAtDesc(staffBuildingIds, pageable).map(this::toResponse);
            }
        } else if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_ASSIGNED")) {
            result = maintenanceRepository.findByAssignedToStaff_User_EmailIgnoreCaseOrderByCreatedAtDesc(authentication.getName(), pageable).map(this::toResponse);
        } else if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_OWN")) {
            result = maintenanceRepository.findByRequestedByUser_EmailIgnoreCaseOrderByCreatedAtDesc(authentication.getName(), pageable).map(this::toResponse);
        } else if (permissionService.hasPermission(authentication, "MAINTENANCE_VIEW_UNIT")) {
            result = maintenanceRepository.findForOwnedUnits(authentication.getName(), pageable).map(this::toResponse);
        } else {
            throw new AccessDeniedException("User is not allowed to view maintenance");
        }
        return MaintenancePageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public MaintenanceResponse getMaintenance(UUID id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public MaintenanceResponse createMaintenance(MaintenanceCreateRequest request, Authentication authentication) {
        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + request.unitId()));
        validateUnitActive(unit);
        validateCreateScope(authentication, unit);

        User requester = getAuthenticatedUser(authentication);
        MaintenanceRequest maintenance = new MaintenanceRequest();
        maintenance.setBuilding(unit.getBuilding());
        maintenance.setUnit(unit);
        maintenance.setRequestedByUser(requester);
        maintenance.setStatus(Enums.MaintenanceStatus.CREATED);
        maintenance.setPriority(request.priority());
        maintenance.setCategory(request.category().trim());
        maintenance.setDescription(request.description().trim());
        maintenance.setScheduledAt(request.scheduledAt());
        maintenance.setUpdatedBy(requester.getId());

        if (request.incidentId() != null) {
            Incident incident = incidentRepository.findById(request.incidentId())
                    .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + request.incidentId()));
            if (!incident.getUnit().getId().equals(unit.getId())) {
                throw new IllegalArgumentException("Maintenance incident must belong to the selected unit");
            }
            maintenance.setIncident(incident);
        }
        MaintenanceRequest saved = maintenanceRepository.save(maintenance);
        auditService.record(authentication.getName(), saved.getBuilding(), "MAINTENANCE", saved.getId(), "CREATE", null, saved.getStatus().name(), null, java.util.Map.of("unitId", saved.getUnit().getId().toString()));
        return toResponse(saved);
    }

    @Transactional
    public MaintenanceResponse updateMaintenance(UUID id, MaintenanceUpdateRequest request, Authentication authentication) {
        MaintenanceRequest maintenance = getEntity(id);
        User currentUser = getAuthenticatedUser(authentication);
        maintenance.setPriority(request.priority());
        maintenance.setCategory(request.category().trim());
        maintenance.setDescription(request.description().trim());
        maintenance.setScheduledAt(request.scheduledAt());
        maintenance.setUpdatedBy(currentUser.getId());
        MaintenanceRequest saved = maintenanceRepository.save(maintenance);
        auditService.record(authentication.getName(), saved.getBuilding(), "MAINTENANCE", saved.getId(), "UPDATE", java.util.Map.of());
        return toResponse(saved);
    }

    @Transactional
    public MaintenanceResponse assignMaintenance(UUID id, MaintenanceAssignmentRequest request, Authentication authentication) {
        MaintenanceRequest maintenance = getEntity(id);
        User currentUser = getAuthenticatedUser(authentication);
        if (request.assignedToStaffId() == null) {
            maintenance.setAssignedToStaff(null);
            if (maintenance.getStatus() == Enums.MaintenanceStatus.ASSIGNED) maintenance.setStatus(Enums.MaintenanceStatus.CREATED);
        } else {
            Staff staff = staffRepository.findByIdAndActiveTrue(request.assignedToStaffId())
                    .orElseThrow(() -> new IllegalArgumentException("Active maintenance staff member not found: " + request.assignedToStaffId()));
            if (!staff.getBuilding().getId().equals(maintenance.getBuilding().getId())) {
                throw new IllegalArgumentException("Assigned staff member must belong to the maintenance building");
            }
            if (staff.getStaffType() != Enums.StaffType.MAINTENANCE) {
                throw new IllegalArgumentException("Assigned staff member must have MAINTENANCE staff type");
            }
            maintenance.setAssignedToStaff(staff);
            if (maintenance.getStatus() == Enums.MaintenanceStatus.CREATED) maintenance.setStatus(Enums.MaintenanceStatus.ASSIGNED);
        }
        maintenance.setUpdatedBy(currentUser.getId());
        MaintenanceRequest saved = maintenanceRepository.save(maintenance);
        auditService.record(authentication.getName(), saved.getBuilding(), "MAINTENANCE", saved.getId(), "ASSIGNED", saved.getStatus().name(), saved.getStatus().name(), null, saved.getAssignedToStaff() == null ? java.util.Map.of() : java.util.Map.of("staffId", saved.getAssignedToStaff().getId().toString()));
        return toResponse(saved);
    }

    @Transactional
    public MaintenanceResponse updateStatus(UUID id, MaintenanceStatusUpdateRequest request, Authentication authentication) {
        MaintenanceRequest maintenance = getEntity(id);
        User currentUser = getAuthenticatedUser(authentication);
        Enums.MaintenanceStatus current = maintenance.getStatus();
        Enums.MaintenanceStatus target = request.status();
        boolean administrator = permissionService.hasPermission(authentication, "MAINTENANCE_UPDATE", maintenance.getBuilding().getId());

        if (current == target) throw new IllegalStateException("Maintenance request is already in status " + target);
        if (!administrator && maintenance.getAssignedToStaff() == null) throw new IllegalStateException("Maintenance request is not assigned");
        if (!administrator && target != Enums.MaintenanceStatus.IN_PROGRESS
                && target != Enums.MaintenanceStatus.ON_HOLD
                && target != Enums.MaintenanceStatus.RESOLVED
                && target != Enums.MaintenanceStatus.CLOSED) {
            throw new IllegalStateException("Providers can only move assigned maintenance through IN_PROGRESS, ON_HOLD, RESOLVED and CLOSED");
        }
        validateTransition(current, target, administrator);
        if (target == Enums.MaintenanceStatus.RESOLVED && (request.resolution() == null || request.resolution().trim().isEmpty())) {
            throw new IllegalArgumentException("A resolution is required when resolving maintenance");
        }
        maintenance.setStatus(target);
        maintenance.setUpdatedBy(currentUser.getId());
        if (request.resolution() != null && !request.resolution().trim().isEmpty()) maintenance.setResolution(request.resolution().trim());
        if (target == Enums.MaintenanceStatus.RESOLVED || target == Enums.MaintenanceStatus.CLOSED) {
            if (maintenance.getCompletedAt() == null) maintenance.setCompletedAt(OffsetDateTime.now());
        } else if (current == Enums.MaintenanceStatus.RESOLVED && target == Enums.MaintenanceStatus.IN_PROGRESS) {
            maintenance.setCompletedAt(null);
        }
        MaintenanceRequest saved = maintenanceRepository.save(maintenance);
        auditService.record(authentication.getName(), saved.getBuilding(), "MAINTENANCE", saved.getId(), "STATUS_CHANGED", current.name(), saved.getStatus().name(), saved.getResolution(), java.util.Map.of());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceUnitOptionResponse> getUnitOptions(Authentication authentication) {
        if (permissionService.hasPermission(authentication, "MAINTENANCE_ASSIGN")) {
            return toUnitOptions(unitRepository.findByActiveTrueOrderByBuildingIdAscUnitNumberAsc());
        }
        if (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE")) {
            List<UUID> buildingIds = staffRepository.findActiveByUserEmailIgnoreCase(authentication.getName()).stream()
                    .map(staff -> staff.getBuilding().getId())
                    .distinct()
                    .filter(buildingId -> permissionService.hasPermission(authentication, "MAINTENANCE_CREATE", buildingId))
                    .toList();
            if (buildingIds.isEmpty() && authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMINISTRATOR".equals(a.getAuthority()))) {
                return toUnitOptions(unitRepository.findByActiveTrueOrderByBuildingIdAscUnitNumberAsc());
            }
            if (buildingIds.isEmpty()) return List.of();
            return toUnitOptions(unitRepository.findByBuildingIdInAndActiveTrueOrderByBuildingIdAscUnitNumberAsc(buildingIds));
        }
        if (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE_OWN")) {
            return toUnitOptions(unitRepository.findActiveUnitsForUser(authentication.getName(), PageRequest.of(0, MAX_PAGE_SIZE)).getContent());
        }
        throw new AccessDeniedException("User is not allowed to create maintenance");
    }

    @Transactional(readOnly = true)
    public List<MaintenanceStaffResponse> getAssignableStaff(UUID buildingId) {
        return staffRepository.findByBuildingIdAndActiveTrueAndStaffTypeOrderByUser_LastNameAscUser_FirstNameAsc(buildingId, Enums.StaffType.MAINTENANCE)
                .stream().map(staff -> new MaintenanceStaffResponse(
                        staff.getId(), staff.getUser().getId(), staff.getBuilding().getId(),
                        staff.getUser().getFirstName(), staff.getUser().getLastName(), staff.getStaffType().name(), staff.getEmployeeCode()))
                .toList();
    }

    private void validateCreateScope(Authentication authentication, Unit unit) {
        UUID buildingId = unit.getBuilding().getId();
        if (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE_OWN", buildingId)) {
            if (!residentRepository.existsActiveResidentForUserAndUnit(authentication.getName(), unit.getId())) {
                throw new AccessDeniedException("User can only create maintenance for their own unit");
            }
            return;
        }
        if (permissionService.hasPermission(authentication, "MAINTENANCE_CREATE", buildingId)
                || permissionService.hasPermission(authentication, "MAINTENANCE_ASSIGN", buildingId)) {
            if (hasRole(authentication, "ADMINISTRATOR")) return;
            if (!staffRepository.existsActiveByUserEmailAndBuildingId(authentication.getName(), buildingId)) {
                throw new AccessDeniedException("Reception user is not assigned to the selected building");
            }
            return;
        }
        throw new AccessDeniedException("User is not allowed to create maintenance for the selected building");
    }

    private boolean hasRole(Authentication authentication, String roleCode) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ("ROLE_" + roleCode).equals(authority.getAuthority()));
    }

    private void validateUnitActive(Unit unit) {
        if (!unit.isActive()) throw new IllegalStateException("Unit is inactive");
        if (unit.getBuilding() == null || !unit.getBuilding().isActive()) throw new IllegalStateException("Unit belongs to an inactive building");
    }

    private void validateTransition(Enums.MaintenanceStatus current, Enums.MaintenanceStatus target, boolean administrator) {
        if (target == Enums.MaintenanceStatus.ASSIGNED && !administrator) throw new IllegalStateException("Only administrators can assign maintenance");
        boolean valid = switch (current) {
            case CREATED -> target == Enums.MaintenanceStatus.ASSIGNED || target == Enums.MaintenanceStatus.CANCELLED;
            case ASSIGNED -> target == Enums.MaintenanceStatus.IN_PROGRESS || target == Enums.MaintenanceStatus.CANCELLED;
            case IN_PROGRESS -> target == Enums.MaintenanceStatus.ON_HOLD || target == Enums.MaintenanceStatus.RESOLVED || target == Enums.MaintenanceStatus.CANCELLED;
            case ON_HOLD -> target == Enums.MaintenanceStatus.IN_PROGRESS || target == Enums.MaintenanceStatus.CANCELLED;
            case RESOLVED -> target == Enums.MaintenanceStatus.CLOSED || target == Enums.MaintenanceStatus.IN_PROGRESS;
            case CLOSED, CANCELLED -> false;
        };
        if (!valid) throw new IllegalStateException("Invalid maintenance status transition from " + current + " to " + target);
    }

    private MaintenanceRequest getEntity(UUID id) { return maintenanceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Maintenance request not found: " + id)); }
    private User getAuthenticatedUser(Authentication authentication) { return userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow(() -> new IllegalStateException("Authenticated user not found")); }
    private List<MaintenanceUnitOptionResponse> toUnitOptions(List<Unit> units) { return units.stream().filter(u -> u.getBuilding()!=null && u.getBuilding().isActive()).map(u -> new MaintenanceUnitOptionResponse(u.getId(), u.getUnitNumber(), u.getBuilding().getId(), u.getBuilding().getCode())).toList(); }
    private MaintenanceResponse toResponse(MaintenanceRequest m) {
        Staff staff = m.getAssignedToStaff();
        User requester = m.getRequestedByUser();
        return new MaintenanceResponse(
                m.getId(), m.getBuilding().getId(), m.getBuilding().getCode(), m.getUnit().getId(), m.getUnit().getUnitNumber(),
                m.getIncident() == null ? null : m.getIncident().getId(), requester == null ? null : requester.getId(),
                requester == null ? null : requester.getFirstName() + " " + requester.getLastName(),
                staff == null ? null : staff.getId(), staff == null ? null : staff.getUser().getFirstName() + " " + staff.getUser().getLastName(),
                staff == null || staff.getStaffType() == null ? null : staff.getStaffType().name(), staff == null ? null : staff.getEmployeeCode(),
                m.getStatus().name(), m.getPriority().name(), m.getCategory(), m.getDescription(), m.getScheduledAt(), m.getCompletedAt(), m.getResolution(), m.getCreatedAt(), m.getUpdatedAt());
    }
}
