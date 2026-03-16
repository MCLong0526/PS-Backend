package com.points.PS_Backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "payment_settings")
public class PaymentSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bankName;

    private String accountName;

    private String accountNumber;

    private String qrCodeUrl;

    private LocalDateTime updateTime;
}