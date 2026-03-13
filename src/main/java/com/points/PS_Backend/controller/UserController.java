package com.points.PS_Backend.controller;

import com.points.PS_Backend.dto.ApiResponse;
import com.points.PS_Backend.dto.LoginRequest;
import com.points.PS_Backend.dto.RegisterRequest;
import com.points.PS_Backend.model.User;
import com.points.PS_Backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse register(@Valid @RequestBody RegisterRequest request) {

        userService.register(request);

        return new ApiResponse(
                200,
                "Register success",
                null,
                null
        );
    }

    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginRequest request) {

        String token = userService.login(
                request.getEmail(),
                request.getPassword()
        );

        return new ApiResponse(
                200,
                "success",
                null,
                token
        );
    }
}