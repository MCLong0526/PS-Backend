package com.points.PS_Backend.controller;

import com.points.PS_Backend.dto.ApiResponse;
import com.points.PS_Backend.dto.RegisterRequest;
import com.points.PS_Backend.model.User;
import com.points.PS_Backend.repository.WalletLogRepository;
import com.points.PS_Backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;
    private final WalletLogRepository walletLogRepository;

    public AdminUserController(UserService userService,
                               WalletLogRepository walletLogRepository){
        this.userService = userService;
        this.walletLogRepository = walletLogRepository;
    }

    private String extractToken(HttpServletRequest request){

        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer ")){
            throw new RuntimeException("Missing token");
        }

        return header.substring(7);
    }

    // GET USERS (pagination)
    @GetMapping
    public ApiResponse getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request){

        String token = extractToken(request);

        userService.validateAdmin(token);

        var users = userService.getUsers(page, size, keyword);

        return new ApiResponse(
                200,
                "success",
                users,
                null
        );
    }

    // GET USER DETAIL
    @GetMapping("/{id}")
    public ApiResponse getUser(
            @PathVariable Long id,
            HttpServletRequest request){

        String token = extractToken(request);

        userService.validateAdmin(token);

        User user = userService.getUserById(id);

        return new ApiResponse(
                200,
                "success",
                user,
                null
        );
    }

    // CREATE USER
    @PostMapping
    public ApiResponse createUser(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest){

        String token = extractToken(httpRequest);

        userService.validateAdmin(token);

        userService.register(request);

        return new ApiResponse(
                200,
                "User created",
                null,
                null
        );
    }

    // UPDATE USER
    @PutMapping("/{id}")
    public ApiResponse updateUser(
            @PathVariable Long id,
            @RequestBody RegisterRequest request,
            HttpServletRequest requestHttp){

        String token = extractToken(requestHttp);

        userService.validateAdmin(token);

        userService.updateUser(id, request);

        return new ApiResponse(
                200,
                "User updated",
                null,
                null
        );
    }

    // DEACTIVATE USER
    @PutMapping("/{id}/deactivate")
    public ApiResponse deactivateUser(
            @PathVariable Long id,
            HttpServletRequest request){

        String token = extractToken(request);

        userService.validateAdmin(token);

        userService.deactivateUser(id);

        return new ApiResponse(
                200,
                "User deactivated",
                null,
                null
        );
    }

    @GetMapping("/{id}/wallet")
    public ApiResponse getUserWalletLogs(
            @PathVariable Long id,
            HttpServletRequest request){

        String token = extractToken(request);

        userService.validateAdmin(token);

        var logs = walletLogRepository.findByUserIdOrderByCreateTimeDesc(id);

        return new ApiResponse(
                200,
                "success",
                logs,
                null
        );
    }
}