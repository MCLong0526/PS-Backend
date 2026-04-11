package com.points.PS_Backend.service;

import com.points.PS_Backend.model.WalletLog;
import com.points.PS_Backend.repository.WalletLogRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletService {

    private final WalletLogRepository walletLogRepository;

    public WalletService(WalletLogRepository walletLogRepository) {
        this.walletLogRepository = walletLogRepository;
    }

    public void recordTransaction(Long userId,
                                  BigDecimal amount,
                                  String type,
                                  String description,
                                  Long relatedUserId){

        WalletLog log = new WalletLog();

        log.setUserId(userId);
        log.setAmount(amount);
        log.setType(type);
        log.setDescription(description);
        log.setRelatedUserId(relatedUserId);
        log.setCreateTime(LocalDateTime.now());

        System.out.println("Recording wallet transaction: " + log);

        walletLogRepository.save(log);
    }


}