package com.marques.estoque.security;

import com.marques.estoque.model.user.User;
import com.marques.estoque.model.user.UserRole;
import com.marques.estoque.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin.username:}")
    private String adminUsername;

    @Value("${app.security.admin.password:}")
    private String adminPassword;

    @Value("${app.security.admin.name:Administrador}")
    private String adminName;

    @Override
    public void run(ApplicationArguments args) {
        if (adminUsername == null || adminUsername.isBlank()) {
            return;
        }

        String trimmedUsername = adminUsername.trim();
        UserDetails existing = userRepository.findByUsername(trimmedUsername);

        if (existing instanceof User existingUser) {
            if (existingUser.getRole() != UserRole.ADMIN) {
                existingUser.setRole(UserRole.ADMIN);
                userRepository.save(existingUser);
                log.info("Conta '{}' promovida para ADMIN automaticamente na inicialização.", trimmedUsername);
            }
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("ADMIN_USERNAME definido ('{}'), mas ADMIN_PASSWORD não foi informada. Conta Root não criada.", trimmedUsername);
            return;
        }

        String resolvedName = (adminName != null && !adminName.isBlank()) ? adminName.trim() : "Administrador";
        User admin = new User(resolvedName, trimmedUsername, passwordEncoder.encode(adminPassword), UserRole.ADMIN);
        userRepository.save(admin);
        log.info("Conta Root ADMIN '{}' criada automaticamente na inicialização.", trimmedUsername);
    }
}
