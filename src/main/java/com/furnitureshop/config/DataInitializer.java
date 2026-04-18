package com.furnitureshop.config;

import com.furnitureshop.model.AppUser;
import com.furnitureshop.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * Guaranteed admin user creation on every startup.
 * This runs AFTER Hibernate creates/updates the schema and AFTER data.sql runs.
 * Acts as a safety net - if data.sql fails for any reason, the admin user is
 * still created correctly with a properly encoded password.
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdminUser(AppUserRepository userRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                AppUser admin = AppUser.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .fullName("Administrator")
                        .email("admin@furnitureshop.com")
                        .role("ROLE_ADMIN")
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(admin);
                System.out.println("✅ Admin user created: admin / admin123");
            } else {
                System.out.println("✅ Admin user already exists.");
            }
        };
    }
}
