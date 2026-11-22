package com.example.learnhub.config;

import com.example.learnhub.entity.*;
import com.example.learnhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@learnhub.com")) {
            userRepository.save(User.builder()
                    .fullName("System Admin")
                    .email("admin@learnhub.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build());
            System.out.println(">>> Default admin created: admin@learnhub.com / admin123");
        }
    }
}
