package com.omfgdevelop.trader_workbook.config;

import com.omfgdevelop.trader_workbook.entity.Role;
import com.omfgdevelop.trader_workbook.entity.User;
import com.omfgdevelop.trader_workbook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(adminProperties.getUsername())) {
            return;
        }

        User admin = new User();
        admin.setUsername(adminProperties.getUsername());
        admin.setPasswordHash(passwordEncoder.encode(adminProperties.getPassword()));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        log.info("Created default admin user: {}", adminProperties.getUsername());
    }
}
