package com.points.PS_Backend.repository;

import com.points.PS_Backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByReferralCode(String referralCode);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContaining(
            String username,
            String email,
            String phone,
            Pageable pageable
    );

}