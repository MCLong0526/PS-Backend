package com.points.PS_Backend.repository;

import com.points.PS_Backend.model.WalletLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletLogRepository extends JpaRepository<WalletLog, Long> {

    List<WalletLog> findByUserIdOrderByCreateTimeDesc(Long userId);

}