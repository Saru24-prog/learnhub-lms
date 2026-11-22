package com.example.learnhub.controller;

import com.example.learnhub.dto.UserResponse;
import com.example.learnhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(Authentication auth) {
        return userService.getByEmail(auth.getName());
    }
}
