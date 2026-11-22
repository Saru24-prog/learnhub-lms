package com.example.learnhub.service;

import com.example.learnhub.dto.UserResponse;
import com.example.learnhub.entity.*;
import com.example.learnhub.exception.ResourceNotFoundException;
import com.example.learnhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse changeRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setRole(role);
        return UserResponse.from(userRepository.save(user));
    }
}
