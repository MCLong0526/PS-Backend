package com.points.PS_Backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lucky_draw_item")
public class LuckyDrawItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long luckyDrawProductId; // lucky draw product
    private Long rewardProductId;    // actual reward product

    private Integer weight;          // probability weight

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Getters and Setters

    public Long getLuckyDrawProductId() {
        return luckyDrawProductId;
    }

    public void setLuckyDrawProductId(Long luckyDrawProductId) {
        this.luckyDrawProductId = luckyDrawProductId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRewardProductId() {
        return rewardProductId;
    }

    public void setRewardProductId(Long rewardProductId) {
        this.rewardProductId = rewardProductId;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}