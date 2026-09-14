package com.condotrack.backend.auth;

import com.condotrack.backend.auth.dto.AuthResponse;
import com.condotrack.backend.auth.dto.LoginRequest;
import com.condotrack.backend.model.User;
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

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
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
