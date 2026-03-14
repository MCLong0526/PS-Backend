package com.points.PS_Backend.controller;

import com.points.PS_Backend.dto.ApiResponse;
import com.points.PS_Backend.dto.LoginRequest;
import com.points.PS_Backend.dto.RegisterRequest;
import com.points.PS_Backend.model.User;
import com.points.PS_Backend.service.UserService;
import com.points.PS_Backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/me")
    public ApiResponse getCurrentUser(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing token");
        }

        String token = header.substring(7);

        Long userId = JwtUtil.getUserIdFromToken(token);

        User user = userService.getUserById(userId);

        return new ApiResponse(
                200,
                "success",
                user,
                null
        );
    }

    @PostMapping("/logout")
    public ApiResponse logout() {

        return new ApiResponse(
                200,
                "Logout success",
                null,
                null
        );
    }

    @GetMapping("/my-qr")
    public ResponseEntity<byte[]> getReferralQr(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing token");
        }

        String token = header.substring(7);

        Long userId = JwtUtil.getUserIdFromToken(token);

        byte[] qr = userService.generateReferralQr(userId);

        return ResponseEntity
                .ok()
                .header("Content-Type", "image/png")
                .body(qr);
    }
}