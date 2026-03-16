package com.points.PS_Backend.repository;

import com.points.PS_Backend.model.WalletLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WalletLogRepository extends JpaRepository<WalletLog, Long> {

    Page<WalletLog> findByUserIdAndCreateTimeBetweenOrderByCreateTimeDesc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}