package com.points.PS_Backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class RequestResponse {

    private Long id;

    private String requestType;

    private String redeemType;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String trackingNumber;

    private Long productId;

    private Long userId;

    // user info
    private String username;

    private String email;

    private String receiptUrl;

    private String paymentMethod;
    private String paymentStatus;

    private String bankName;
    private String bankAccount;
    private String bankHolder;

    private LocalDateTime completedTime;

    // address
    private Long addressId;
    private String receiverName;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postcode;
    private String country;

    private Integer amountPoints;
    private BigDecimal amountWallet;
}