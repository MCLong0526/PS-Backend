package com.points.PS_Backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="product_requests")
@Data
public class ProductRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long productId;

    private String redeemType;

    private BigDecimal amountWallet;

    private Integer amountPoints;

    private String status;

    private String trackingNumber;

    private LocalDateTime createTime;

    private LocalDateTime shippedTime;

    private LocalDateTime completedTime;

}