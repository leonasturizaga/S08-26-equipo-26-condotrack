package com.condotrack.backend.service;

import com.condotrack.backend.dto.UserCreateRequest;
import com.condotrack.backend.dto.UserPageResponse;
import com.condotrack.backend.dto.UserResponse;
import com.condotrack.backend.dto.UserRolesUpdateRequest;
import com.condotrack.backend.dto.UserStatusUpdateRequest;
import com.condotrack.backend.dto.UserUpdateRequest;
import com.condotrack.backend.model.Role;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.RoleRepository;
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

        return toResponse(userRepository.save(user));
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

        return toResponse(userRepository.save(user));
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

        return toResponse(userRepository.save(user));
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

        return toResponse(userRepository.save(user));
    }

    public UUID getAuthenticatedUserId(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user is required");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }

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
