package com.points.PS_Backend.controller;

import com.points.PS_Backend.dto.ApiResponse;
import com.points.PS_Backend.model.PaymentSetting;
import com.points.PS_Backend.repository.PaymentSettingRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentSettingRepository paymentSettingRepository;

    public PaymentController(PaymentSettingRepository paymentSettingRepository) {
        this.paymentSettingRepository = paymentSettingRepository;
    }

    @GetMapping("/settings")
    @Cacheable("paymentSettings")
    public ApiResponse getPaymentSettings(){

        PaymentSetting setting = paymentSettingRepository
                .findById(1L)
                .orElse(null);

        return new ApiResponse(
                200,
                "success",
                setting,
                null
        );
    }
}