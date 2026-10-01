//------------------- milestone 20 ---------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.StaffAssignmentRequest;
import com.condotrack.backend.dto.StaffAssignmentResponse;
import com.condotrack.backend.dto.UserCreateRequest;
import com.condotrack.backend.dto.UserPageResponse;
import com.condotrack.backend.dto.UserResponse;
import com.condotrack.backend.dto.UserRolesUpdateRequest;
import com.condotrack.backend.dto.UserStatusUpdateRequest;
import com.condotrack.backend.dto.UserUpdateRequest;
import com.condotrack.backend.model.Building;
import com.condotrack.backend.model.Role;
import com.condotrack.backend.model.Staff;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.BuildingRepository;
import com.condotrack.backend.repository.RoleRepository;
import com.condotrack.backend.repository.StaffRepository;
import com.condotrack.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
private final StaffRepository staffRepository;
private final BuildingRepository buildingRepository;


    @Transactional(readOnly = true)
    public UserPageResponse getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE)
        );

        Page<UserResponse> users = userRepository
                .findAllByOrderByLastNameAscFirstNameAsc(pageable)
                .map(this::toResponse);

        return UserPageResponse.from(users);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        return toResponse(findUser(userId));
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request, UUID actingUserId) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("An account with this email already exists");
        }

        Set<Role> roles = resolveRoles(request.roles());

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(normalizeOptional(request.phone()));
        user.setActive(true);
        user.setRoles(roles);
        user.setUpdatedBy(actingUserId);

        User saved = userRepository.save(user);
        auditService.record(findEmailById(actingUserId), null, "USER", saved.getId(), "CREATE", java.util.Map.of("email", saved.getEmail()));
        return toResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(
            UUID userId,
            UserUpdateRequest request,
            UUID actingUserId
    ) {
        User user = findUser(userId);
        String email = normalizeEmail(request.email());

        userRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new IllegalStateException("An account with this email already exists");
                });

        user.setEmail(email);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(normalizeOptional(request.phone()));
        user.setUpdatedBy(actingUserId);

        User saved = userRepository.save(user);
        auditService.record(findEmailById(actingUserId), null, "USER", saved.getId(), "UPDATE", java.util.Map.of("email", saved.getEmail()));
        return toResponse(saved);
    }

    @Transactional
    public UserResponse updateRoles(
            UUID userId,
            UserRolesUpdateRequest request,
            UUID actingUserId
    ) {
        User user = findUser(userId);
        Set<Role> roles = resolveRoles(request.roles());

        if (userId.equals(actingUserId)
                && roles.stream().noneMatch(role -> "ADMINISTRATOR".equals(role.getCode()))) {
            throw new IllegalStateException("An administrator cannot remove their own ADMINISTRATOR role");
        }

        user.setRoles(new HashSet<>(roles));
        user.setUpdatedBy(actingUserId);

        User saved = userRepository.save(user);
        auditService.record(findEmailById(actingUserId), null, "USER", saved.getId(), "ROLE_CHANGED", java.util.Map.of("roles", saved.getRoles().stream().map(Role::getCode).sorted().toList()));
        return toResponse(saved);
    }

@Transactional
public StaffAssignmentResponse saveStaffAssignment(
        UUID userId,
        StaffAssignmentRequest request,
        UUID actingUserId
) {
    User user = findUser(userId);

    Building building = buildingRepository.findById(request.buildingId())
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Building not found: " + request.buildingId()
                    )
            );

    if (!building.isActive()) {
        throw new IllegalArgumentException(
                "Building is inactive: " + request.buildingId()
        );
    }

    Staff staff = staffRepository
            .findFirstByUser_IdOrderByActiveDescCreatedAtDesc(userId)
            .orElseGet(Staff::new);

    boolean creating = staff.getId() == null;

    staff.setUser(user);
    staff.setBuilding(building);
    staff.setStaffType(request.staffType());
    staff.setEmployeeCode(
            request.employeeCode() == null
                    ? null
                    : request.employeeCode().trim().isEmpty()
                        ? null
                        : request.employeeCode().trim()
    );
    staff.setActive(
            request.active() == null
                    || request.active()
    );

    if (creating) {
        staff.setUpdatedBy(actingUserId);
    } else {
        staff.setUpdatedBy(actingUserId);
    }

    Staff saved = staffRepository.save(staff);

    auditService.record(
            findEmailById(actingUserId),
            building,
            "STAFF",
            saved.getId(),
            creating ? "CREATED" : "UPDATED",
            null,
            saved.getStaffType().name(),
            null,
            java.util.Map.of(
                    "userId", userId.toString(),
                    "buildingId", building.getId().toString(),
                    "staffType", saved.getStaffType().name(),
                    "active", saved.isActive()
            )
    );

    return new StaffAssignmentResponse(
            saved.getId(),
            saved.getUser().getId(),
            saved.getBuilding().getId(),
            saved.getBuilding().getCode(),
            saved.getStaffType(),
            saved.getEmployeeCode(),
            saved.isActive()
    );
}





    @Transactional
    public UserResponse updateStatus(
            UUID userId,
            UserStatusUpdateRequest request,
            UUID actingUserId
    ) {
        User user = findUser(userId);

        if (userId.equals(actingUserId) && !request.active()) {
            throw new IllegalStateException("An administrator cannot deactivate their own account");
        }

        user.setActive(request.active());
        user.setUpdatedBy(actingUserId);

        User saved = userRepository.save(user);
        auditService.record(findEmailById(actingUserId), null, "USER", saved.getId(), "STATUS_CHANGED", null, Boolean.toString(saved.isActive()));
        return toResponse(saved);
    }

    public UUID getAuthenticatedUserId(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user is required");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }

    private String findEmailById(UUID userId) { return userRepository.findById(userId).map(User::getEmail).orElse(null); }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private Set<Role> resolveRoles(Set<String> requestedRoles) {
        Set<String> normalizedCodes = requestedRoles.stream()
                .map(this::normalizeRoleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Role> roles = new ArrayList<>();
        for (String code : normalizedCodes) {
            roles.add(roleRepository.findByCode(code)
                    .orElseThrow(() -> new IllegalStateException("Role not found: " + code)));
        }

        return new HashSet<>(roles);
    }

    private UserResponse toResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .sorted(Comparator.naturalOrder())
                .toList();

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.isActive(),
                roles
        );
    }

    private String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRoleCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
