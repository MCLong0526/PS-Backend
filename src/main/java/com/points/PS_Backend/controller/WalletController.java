package com.points.PS_Backend.controller;

import com.points.PS_Backend.dto.ApiResponse;
import com.points.PS_Backend.model.WalletLog;
import com.points.PS_Backend.repository.WalletLogRepository;
import com.points.PS_Backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletLogRepository walletLogRepository;

    public WalletController(WalletLogRepository walletLogRepository) {
        this.walletLogRepository = walletLogRepository;
    }

    private String extractToken(HttpServletRequest request){

        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer ")){
            throw new RuntimeException("Missing token");
        }

        return header.substring(7);
    }

    @GetMapping("/history")
    public ApiResponse getWalletHistory(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate){

        String token = extractToken(request);

        Long userId = JwtUtil.getUserIdFromToken(token);

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

        var history = walletLogRepository
                .findByUserIdAndCreateTimeBetweenOrderByCreateTimeDesc(
                        userId,
                        start,
                        end,
                        pageable
                );

        return new ApiResponse(
                200,
                "success",
                history,
                null
        );
    }
}