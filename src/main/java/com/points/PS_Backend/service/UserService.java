package com.points.PS_Backend.service;

import com.points.PS_Backend.dto.RegisterRequest;
import com.points.PS_Backend.model.User;
import com.points.PS_Backend.repository.UserRepository;
import com.points.PS_Backend.utils.JwtUtil;
import com.points.PS_Backend.utils.PasswordUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(RegisterRequest request) {

        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already registered");
        }

        if(userRepository.existsByPhone(request.getPhone())){
            throw new RuntimeException("Phone already registered");
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user.setPassword(
                PasswordUtil.hashPassword(request.getPassword())
        );

        user.setPoints(0);
        user.setCreateTime(LocalDateTime.now());

        userRepository.save(user);
    }

    public String login(String email, String password) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!PasswordUtil.matchPassword(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return JwtUtil.generateToken(user.getId());
    }

}