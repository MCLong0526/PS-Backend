package com.points.PS_Backend.controller;

import com.points.PS_Backend.dto.ApiResponse;
import com.points.PS_Backend.dto.RegisterRequest;
import com.points.PS_Backend.model.PaymentSetting;
import com.points.PS_Backend.model.User;
import com.points.PS_Backend.repository.PaymentSettingRepository;
import com.points.PS_Backend.repository.WalletLogRepository;
import com.points.PS_Backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;
    private final PaymentSettingRepository paymentSettingRepository;
    private final WalletLogRepository walletLogRepository;

    public AdminUserController(UserService userService,
                               PaymentSettingRepository paymentSettingRepository,
                               WalletLogRepository walletLogRepository){
        this.userService = userService;
        this.paymentSettingRepository = paymentSettingRepository;
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

    @GetMapping("/{id}/wallet")
    public ApiResponse getUserWalletLogs(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate){

        String token = extractToken(request);

        userService.validateAdmin(token);

        Pageable pageable = PageRequest.of(page, size);

        LocalDateTime start;
        LocalDateTime end;

        if(startDate != null && endDate != null){
            start = LocalDate.parse(startDate).atStartOfDay();
            end = LocalDate.parse(endDate).atTime(23,59,59);
        }else{
            start = LocalDate.of(2000,1,1).atStartOfDay();
            end = LocalDate.now().atTime(23,59,59);
        }

        var logs = walletLogRepository
                .findByUserIdAndCreateTimeBetweenOrderByCreateTimeDesc(
                        id,
                        start,
                        end,
                        pageable
                );

        return new ApiResponse(
                200,
                "success",
                logs,
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
            @RequestBody RegisterRequest body,
            HttpServletRequest request){

        String token = extractToken(request);

        userService.validateAdmin(token);

        userService.register(body);

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



    @PutMapping("/settings")
    public ApiResponse updatePaymentSettings(
            @RequestBody PaymentSetting setting,
            HttpServletRequest request){

        String token = extractToken(request);
        userService.validateAdmin(token);

        PaymentSetting existing = paymentSettingRepository
                .findById(1L)
                .orElse(null);

        if(existing == null){
            existing = new PaymentSetting();
        }

        existing.setBankName(setting.getBankName());
        existing.setAccountName(setting.getAccountName());
        existing.setAccountNumber(setting.getAccountNumber());
        existing.setQrCodeUrl(setting.getQrCodeUrl());
        existing.setUpdateTime(LocalDateTime.now());

        paymentSettingRepository.save(existing);

        return new ApiResponse(200,"Payment settings updated",existing,null);
    }

    @GetMapping("/payment-settings")
    public ApiResponse getPaymentSettings(HttpServletRequest request){

        String token = extractToken(request);
        userService.validateAdmin(token);

        PaymentSetting setting = paymentSettingRepository
                .findById(1L)
                .orElse(null);

        return new ApiResponse(200,"success",setting,null);
    }

    @PostMapping("/payment-qr")
    public ApiResponse uploadQrCode(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {

        String token = extractToken(request);
        userService.validateAdmin(token);

        String uploadDir = System.getProperty("user.dir") + "/uploads/payment/";

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File dest = new File(uploadDir + filename);

        file.transferTo(dest);

        String url = "/uploads/payment/" + filename;

        // 🔹 Save QR URL into payment setting
        PaymentSetting setting = paymentSettingRepository
                .findById(1L)
                .orElse(new PaymentSetting());

        setting.setQrCodeUrl(url);
        setting.setUpdateTime(LocalDateTime.now());

        paymentSettingRepository.save(setting);

        return new ApiResponse(
                200,
                "Upload success",
                url,
                null
        );
    }
}