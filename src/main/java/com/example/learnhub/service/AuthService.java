package com.example.learnhub.service;

import com.example.learnhub.dto.*;
import com.example.learnhub.entity.*;
import com.example.learnhub.exception.DuplicateResourceException;
import com.example.learnhub.repository.UserRepository;
import com.example.learnhub.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered");
        }
        User user = User.builder()
                .fullName(req.getFullName())
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.STUDENT)          // new users are always students
                .build();
        userRepository.save(user);
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return toResponse(user);
    }

    private AuthResponse toResponse(User u) {
        return new AuthResponse(jwtService.generateToken(u), u.getFullName(), u.getEmail(), u.getRole().name());
    }
}
