package com.points.PS_Backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private BigDecimal priceWallet;

    private Integer pricePoints;

    private Integer quantity;

    private String imageUrl;

    private String status;

    @Transient
    private List<ProductImage> images;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}