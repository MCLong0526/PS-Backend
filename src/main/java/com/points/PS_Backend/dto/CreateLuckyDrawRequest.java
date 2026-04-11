package com.points.PS_Backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateLuckyDrawRequest {

    private String name;
    private String description;
    private BigDecimal priceWallet;
    private Integer pricePoints;
    private Integer quantity;
    private String productType;

    private List<RewardItem> rewards;

    @Data
    public static class RewardItem {
        private Long rewardProductId;
        private Integer weight;
    }
}