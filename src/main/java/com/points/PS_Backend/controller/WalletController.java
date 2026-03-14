package com.points.PS_Backend.controller;

import com.points.PS_Backend.dto.ApiResponse;
import com.points.PS_Backend.dto.WalletLogResponse;
import com.points.PS_Backend.model.WalletLog;
import com.points.PS_Backend.repository.WalletLogRepository;
import com.points.PS_Backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletLogRepository walletLogRepository;

    public WalletController(WalletLogRepository walletLogRepository) {
        this.walletLogRepository = walletLogRepository;
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(HttpServletRequest request){

        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer ")){
            throw new RuntimeException("Missing token");
        }

        return header.substring(7);
    }

    /**
     * USER - Get own wallet history
     */
    @GetMapping("/history")
    public ApiResponse getWalletHistory(HttpServletRequest request){

        String token = extractToken(request);

        Long userId = JwtUtil.getUserIdFromToken(token);

        List<WalletLog> logs =
                walletLogRepository.findByUserIdOrderByCreateTimeDesc(userId);

        // Convert entity -> DTO
        List<WalletLogResponse> response = logs.stream().map(log -> {

            WalletLogResponse dto = new WalletLogResponse();

            dto.setAmount(log.getAmount());
            dto.setType(log.getType());
            dto.setDescription(log.getDescription());
            dto.setCreateTime(log.getCreateTime());

            return dto;

        }).collect(Collectors.toList());

        return new ApiResponse(
                200,
                "success",
                response,
                null
        );
    }
}