package com.condotrack.backend.auth;

import com.condotrack.backend.auth.dto.AuthResponse;
import com.condotrack.backend.auth.dto.LoginRequest;
import com.condotrack.backend.auth.dto.RegisterRequest;
import com.condotrack.backend.auth.dto.RegistrationResponse;
import com.condotrack.backend.model.Role;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.condotrack.backend.repository.UserRepository;
import com.condotrack.backend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user could not be loaded"));

        return buildResponse(user, jwtService.generateToken(user));
    }


    @Transactional
    public RegistrationResponse registerResident(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalStateException("An account with this email already exists");
        }

        Role residentRole = roleRepository.findByCode("RESIDENT")
                .orElseThrow(() -> new IllegalStateException("RESIDENT role was not found in the database"));

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(normalizeOptional(request.phone()));
        user.setActive(true);
        user.getRoles().add(residentRole);

        User savedUser = userRepository.save(user);

        return new RegistrationResponse(
                "Resident account created successfully",
                buildResponse(savedUser, null).user()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("User could not be loaded"));

        return buildResponse(user, null);
    }

    private AuthResponse buildResponse(User user, String token) {
        var roles = user.getRoles()
                .stream()
                .map(role -> role.getCode())
                .sorted(Comparator.naturalOrder())
                .toList();

        AuthResponse.UserResponse userResponse = new AuthResponse.UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                roles
        );

        return new AuthResponse(token, userResponse);
    }
}
