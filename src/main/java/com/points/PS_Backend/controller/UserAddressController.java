package com.points.PS_Backend.controller;

import com.points.PS_Backend.dto.ApiResponse;
import com.points.PS_Backend.model.UserAddress;
import com.points.PS_Backend.repository.UserAddressRepository;
import com.points.PS_Backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class UserAddressController {

    private final UserAddressRepository userAddressRepository;

    public UserAddressController(UserAddressRepository userAddressRepository) {
        this.userAddressRepository = userAddressRepository;
    }

    private Long getUserId(HttpServletRequest request){

        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer ")){
            throw new RuntimeException("Missing token");
        }

        String token = header.substring(7);

        return JwtUtil.getUserIdFromToken(token);
    }

    @GetMapping
    public ApiResponse getAddresses(HttpServletRequest request){

        Long userId = getUserId(request);

        List<UserAddress> addresses = userAddressRepository.findByUserId(userId);

        return new ApiResponse(200,"success",addresses,null);
    }

    @PostMapping
    public ApiResponse createAddress(
            @RequestBody UserAddress address,
            HttpServletRequest request){

        Long userId = getUserId(request);

        address.setUserId(userId);
        address.setCreateTime(LocalDateTime.now());

        userAddressRepository.save(address);

        return new ApiResponse(200,"Address created",address,null);
    }
}