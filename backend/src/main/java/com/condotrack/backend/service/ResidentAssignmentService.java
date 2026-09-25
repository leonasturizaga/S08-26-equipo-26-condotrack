//--------------------- milestone 19 -----------------------
// package com.condotrack.backend.service;

// import com.condotrack.backend.dto.ResidentCreateRequest;
// import com.condotrack.backend.dto.ResidentResponse;
// import com.condotrack.backend.dto.ResidentUnitAssignmentUpdateRequest;
// import com.condotrack.backend.model.Enums;
// import com.condotrack.backend.model.Resident;
// import com.condotrack.backend.model.Unit;
// import com.condotrack.backend.model.User;
// import com.condotrack.backend.repository.ResidentRepository;
// import com.condotrack.backend.repository.UnitRepository;
// import com.condotrack.backend.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDate;
// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// public class ResidentAssignmentService {

//     private final ResidentRepository residentRepository;
//     private final UserRepository userRepository;
//     private final UnitRepository unitRepository;

//     @Transactional(readOnly = true)
//     public UUID getAuthenticatedUserId(Authentication authentication) {
//         if (authentication == null || authentication.getName() == null) {
//             throw new IllegalStateException("Authenticated user is required");
//         }

//         return userRepository.findByEmailIgnoreCase(authentication.getName())
//                 .map(User::getId)
//                 .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
//     }

//     @Transactional
//     public ResidentResponse assignResident(
//             ResidentCreateRequest request,
//             UUID updatedBy
//     ) {
//         User user = getActiveUser(request.userId());
//         validateResidentUserRole(user);
//         Unit unit = getActiveUnit(request.unitId());
//         Enums.ResidentType residentType = parseResidentType(request.residentType());
//         validateDates(request.moveInDate(), request.moveOutDate());

//         if (residentRepository.existsActiveResidentForUserAndUnit(
//                 user.getEmail(),
//                 unit.getId()
//         )) {
//             throw new IllegalStateException(
//                     "The user is already actively assigned to this unit"
//             );
//         }

//         Resident resident = new Resident();
//         resident.setUser(user);
//         resident.setUnit(unit);
//         resident.setResidentType(residentType);
//         resident.setMoveInDate(request.moveInDate());
//         resident.setMoveOutDate(request.moveOutDate());
//         resident.setPrimaryContact(request.primaryContact());
//         resident.setActive(true);
//         resident.setUpdatedBy(updatedBy);

//         return toResponse(residentRepository.save(resident));
//     }

//     @Transactional
//     public ResidentResponse reassignResident(
//             UUID residentId,
//             ResidentUnitAssignmentUpdateRequest request,
//             UUID updatedBy
//     ) {
//         Resident resident = residentRepository.findById(residentId)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Resident not found: " + residentId
//                 ));

//         if (!resident.isActive()) {
//             throw new IllegalArgumentException(
//                     "Only an active resident can be reassigned"
//             );
//         }

//         Unit targetUnit = getActiveUnit(request.unitId());

//         if (resident.getUnit().getId().equals(targetUnit.getId())) {
//             throw new IllegalStateException(
//                     "The resident is already assigned to this unit"
//             );
//         }

//         if (residentRepository.existsActiveResidentForUserAndUnit(
//                 resident.getUser().getEmail(),
//                 targetUnit.getId()
//         )) {
//             throw new IllegalStateException(
//                     "The user is already actively assigned to the target unit"
//             );
//         }

//         resident.setUnit(targetUnit);
//         resident.setUpdatedBy(updatedBy);

//         return toResponse(residentRepository.save(resident));
//     }

//     private User getActiveUser(UUID userId) {
//         User user = userRepository.findById(userId)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "User not found: " + userId
//                 ));

//         if (!user.isActive()) {
//             throw new IllegalArgumentException(
//                     "User is inactive: " + userId
//             );
//         }

//         return user;
//     }

//     private void validateResidentUserRole(User user) {
//         boolean eligible = user.getRoles().stream()
//                 .map(role -> role.getCode())
//                 .anyMatch(code -> "RESIDENT".equals(code) || "OWNER".equals(code));

//         if (!eligible) {
//             throw new IllegalArgumentException(
//                     "The selected user must have the RESIDENT or OWNER role"
//             );
//         }
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

//     private Enums.ResidentType parseResidentType(String value) {
//         try {
//             return Enums.ResidentType.valueOf(value.trim().toUpperCase());
//         } catch (Exception exception) {
//             throw new IllegalArgumentException(
//                     "Invalid residentType. Allowed values: OWNER, TENANT, OCCUPANT, AUTHORIZED_RESIDENT"
//             );
//         }
//     }

//     private void validateDates(LocalDate moveInDate, LocalDate moveOutDate) {
//         if (moveInDate != null && moveOutDate != null && moveOutDate.isBefore(moveInDate)) {
//             throw new IllegalArgumentException(
//                     "moveOutDate cannot be before moveInDate"
//             );
//         }
//     }

//     private ResidentResponse toResponse(Resident resident) {
//         return new ResidentResponse(
//                 resident.getId(),
//                 resident.getUser().getId(),
//                 resident.getUser().getEmail(),
//                 resident.getUser().getFirstName(),
//                 resident.getUser().getLastName(),
//                 resident.getUser().getPhone(),
//                 resident.getUnit().getId(),
//                 resident.getUnit().getBuilding().getId(),
//                 resident.getUnit().getUnitNumber(),
//                 resident.getResidentType().name(),
//                 resident.getMoveInDate() == null ? null : resident.getMoveInDate().toString(),
//                 resident.getMoveOutDate() == null ? null : resident.getMoveOutDate().toString(),
//                 resident.isPrimaryContact(),
//                 resident.isActive()
//         );
//     }
// }


//------------------- milestone 20 ---------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.ResidentCreateRequest;
import com.condotrack.backend.dto.ResidentResponse;
import com.condotrack.backend.dto.ResidentUnitAssignmentUpdateRequest;
import com.condotrack.backend.model.Enums;
import com.condotrack.backend.model.Resident;
import com.condotrack.backend.model.Unit;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.ResidentRepository;
import com.condotrack.backend.repository.UnitRepository;
import com.condotrack.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResidentAssignmentService {

    private final ResidentRepository residentRepository;
    private final UserRepository userRepository;
    private final UnitRepository unitRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public UUID getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user is required");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    @Transactional
    public ResidentResponse assignResident(
            ResidentCreateRequest request,
            UUID updatedBy
    ) {
        User user = getActiveUser(request.userId());
        validateResidentUserRole(user);
        Unit unit = getActiveUnit(request.unitId());
        Enums.ResidentType residentType = parseResidentType(request.residentType());
        validateDates(request.moveInDate(), request.moveOutDate());

        if (residentRepository.existsActiveResidentForUserAndUnit(
                user.getEmail(),
                unit.getId()
        )) {
            throw new IllegalStateException(
                    "The user is already actively assigned to this unit"
            );
        }

        Resident resident = new Resident();
        resident.setUser(user);
        resident.setUnit(unit);
        resident.setResidentType(residentType);
        resident.setMoveInDate(request.moveInDate());
        resident.setMoveOutDate(request.moveOutDate());
        resident.setPrimaryContact(request.primaryContact());
        resident.setActive(true);
        resident.setUpdatedBy(updatedBy);

        Resident saved = residentRepository.save(resident);
        auditService.record(findActorEmail(updatedBy), unit.getBuilding(), "RESIDENT", saved.getId(), "CREATE", java.util.Map.of("userId", user.getId().toString(), "unitId", unit.getId().toString()));
        return toResponse(saved);
    }

    @Transactional
    public ResidentResponse reassignResident(
            UUID residentId,
            ResidentUnitAssignmentUpdateRequest request,
            UUID updatedBy
    ) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Resident not found: " + residentId
                ));

        if (!resident.isActive()) {
            throw new IllegalArgumentException(
                    "Only an active resident can be reassigned"
            );
        }

        Unit targetUnit = getActiveUnit(request.unitId());

        if (resident.getUnit().getId().equals(targetUnit.getId())) {
            throw new IllegalStateException(
                    "The resident is already assigned to this unit"
            );
        }

        if (residentRepository.existsActiveResidentForUserAndUnit(
                resident.getUser().getEmail(),
                targetUnit.getId()
        )) {
            throw new IllegalStateException(
                    "The user is already actively assigned to the target unit"
            );
        }

        Unit previousUnit = resident.getUnit();
        resident.setUnit(targetUnit);
        resident.setUpdatedBy(updatedBy);

        Resident saved = residentRepository.save(resident);
        auditService.record(findActorEmail(updatedBy), targetUnit.getBuilding(), "RESIDENT", saved.getId(), "UNIT_REASSIGNED", java.util.Map.of("fromUnitId", previousUnit.getId().toString(), "toUnitId", targetUnit.getId().toString()));
        return toResponse(saved);
    }

    private String findActorEmail(UUID actorId) { return userRepository.findById(actorId).map(User::getEmail).orElse(null); }

    private User getActiveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + userId
                ));

        if (!user.isActive()) {
            throw new IllegalArgumentException(
                    "User is inactive: " + userId
            );
        }

        return user;
    }

    private void validateResidentUserRole(User user) {
        boolean eligible = user.getRoles().stream()
                .map(role -> role.getCode())
                .anyMatch(code -> "RESIDENT".equals(code) || "OWNER".equals(code));

        if (!eligible) {
            throw new IllegalArgumentException(
                    "The selected user must have the RESIDENT or OWNER role"
            );
        }
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

    private Enums.ResidentType parseResidentType(String value) {
        try {
            return Enums.ResidentType.valueOf(value.trim().toUpperCase());
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid residentType. Allowed values: OWNER, TENANT, OCCUPANT, AUTHORIZED_RESIDENT"
            );
        }
    }

    private void validateDates(LocalDate moveInDate, LocalDate moveOutDate) {
        if (moveInDate != null && moveOutDate != null && moveOutDate.isBefore(moveInDate)) {
            throw new IllegalArgumentException(
                    "moveOutDate cannot be before moveInDate"
            );
        }
    }

    private ResidentResponse toResponse(Resident resident) {
        return new ResidentResponse(
                resident.getId(),
                resident.getUser().getId(),
                resident.getUser().getEmail(),
                resident.getUser().getFirstName(),
                resident.getUser().getLastName(),
                resident.getUser().getPhone(),
                resident.getUnit().getId(),
                resident.getUnit().getBuilding().getId(),
                resident.getUnit().getUnitNumber(),
                resident.getResidentType().name(),
                resident.getMoveInDate() == null ? null : resident.getMoveInDate().toString(),
                resident.getMoveOutDate() == null ? null : resident.getMoveOutDate().toString(),
                resident.isPrimaryContact(),
                resident.isActive()
        );
    }
}
