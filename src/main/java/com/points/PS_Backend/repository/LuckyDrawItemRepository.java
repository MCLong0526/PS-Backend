package com.points.PS_Backend.repository;

import com.points.PS_Backend.model.LuckyDrawItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LuckyDrawItemRepository extends JpaRepository<LuckyDrawItem, Long> {

    List<LuckyDrawItem> findByLuckyDrawProductId(Long luckyDrawProductId);

    boolean existsByLuckyDrawProductIdAndRewardProductId(Long luckyDrawProductId, Long rewardProductId);
}