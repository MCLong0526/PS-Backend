package com.points.PS_Backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="requests")
@Data
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String requestType;

    private Long productId;

    private String redeemType;

    private BigDecimal amountWallet;

    private Integer amountPoints;

    private String bankName;
    private String bankAccount;
    private String bankHolder;

    private String trackingNumber;

    @Column(name = "receipt_url")
    private String receiptUrl;

    private String status;

    private Long addressId;

    private String paymentMethod;

    private String paymentStatus;

    private Long sourceProductId;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime completedTime;
}