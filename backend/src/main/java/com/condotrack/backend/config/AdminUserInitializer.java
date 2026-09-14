package com.condotrack.backend.config;

import com.condotrack.backend.model.Role;
import com.condotrack.backend.model.User;
import com.condotrack.backend.repository.RoleRepository;
import com.condotrack.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.bootstrap-admin.email:}")
    private String adminEmail;

    @Value("${security.bootstrap-admin.password:}")
    private String adminPassword;

    public AdminUserInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            log.warn("Bootstrap administrator is disabled because security.bootstrap-admin.email/password are not configured.");
            return;
        }

        String normalizedEmail = adminEmail.trim().toLowerCase();

        Role administratorRole = roleRepository.findByCode("ADMINISTRATOR")
                .orElseThrow(() -> new IllegalStateException("ADMINISTRATOR role was not found in the database"));

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(normalizedEmail);
                    newUser.setFirstName("System");
                    newUser.setLastName("Administrator");
                    newUser.setPhone(null);
                    newUser.setPasswordHash(passwordEncoder.encode(adminPassword));
                    newUser.setActive(true);
                    return newUser;
                });

        user.getRoles().add(administratorRole);

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(adminPassword));
        }

        user.setActive(true);
        userRepository.save(user);

        log.info("Bootstrap administrator is ready: {}", normalizedEmail);
    }
}
