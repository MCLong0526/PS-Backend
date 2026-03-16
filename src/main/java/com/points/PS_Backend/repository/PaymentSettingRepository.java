package com.points.PS_Backend.repository;

import com.points.PS_Backend.model.PaymentSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentSettingRepository extends JpaRepository<PaymentSetting, Long> {

}