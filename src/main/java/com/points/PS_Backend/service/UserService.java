package com.points.PS_Backend.service;

import com.points.PS_Backend.dto.RegisterRequest;
import com.points.PS_Backend.model.User;
import com.points.PS_Backend.repository.UserRepository;
import com.points.PS_Backend.utils.JwtUtil;
import com.points.PS_Backend.utils.PasswordUtil;
import com.points.PS_Backend.utils.QrCodeUtil;
import com.points.PS_Backend.utils.ReferralUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final WalletService walletService;

    private static final BigDecimal LEVEL1_REWARD = new BigDecimal("2");
    private static final BigDecimal LEVEL2_REWARD = new BigDecimal("1");

    public UserService(UserRepository userRepository,
                       WalletService walletService) {
        this.userRepository = userRepository;
        this.walletService = walletService;
    }

    public void register(RegisterRequest request) {

        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already registered");
        }

        if(userRepository.existsByPhone(request.getPhone())){
            throw new RuntimeException("Phone already registered");
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user.setPassword(
                PasswordUtil.hashPassword(request.getPassword())
        );

        user.setRole("CUSTOMER");

        user.setReferralCode(
                ReferralUtil.generateCode()
        );

        user.setPoints(0);
        user.setWallet(BigDecimal.ZERO);
        user.setCreateTime(LocalDateTime.now());
        user.setStatus("ACTIVE");

        // Handle referral
        // Handle referral
        if(request.getReferralCode() != null){

            User inviter = userRepository
                    .findByReferralCode(request.getReferralCode())
                    .orElseThrow(() -> new RuntimeException("Invalid referral code"));

            user.setInvitedBy(inviter.getId());

            // Level 1 reward
            inviter.setWallet(
                    inviter.getWallet().add(LEVEL1_REWARD)
            );

            walletService.recordTransaction(
                    inviter.getId(),
                    LEVEL1_REWARD,
                    "REFERRAL_LEVEL1",
                    "Referral reward",
                    user.getId()
            );

            userRepository.save(inviter);

            // Level 2 reward
            if(inviter.getInvitedBy() != null){

                User level2 = userRepository
                        .findById(inviter.getInvitedBy())
                        .orElse(null);

                if(level2 != null){

                    level2.setWallet(
                            level2.getWallet().add(LEVEL2_REWARD)
                    );

                    userRepository.save(level2);

                    walletService.recordTransaction(
                            level2.getId(),
                            LEVEL2_REWARD,
                            "REFERRAL_LEVEL2",
                            "Referral reward",
                            user.getId()
                    );
                }
            }
        }

        userRepository.save(user);
    }

    public String login(String email, String password) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!PasswordUtil.matchPassword(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if(!"ACTIVE".equals(user.getStatus())){
            throw new RuntimeException("User is deactivated");
        }

        return JwtUtil.generateToken(user.getId());
    }

    public User getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public byte[] generateReferralQr(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String referralCode = user.getReferralCode();

        String inviteLink =
                "http://localhost:5500/register.html?ref=" + referralCode;

        return QrCodeUtil.generateQRCode(inviteLink);
    }


    // ADMIN - GET ALL USERS
    public Page<User> getUsers(int page, int size, String keyword){

        Pageable pageable = PageRequest.of(page, size);

        if(keyword == null || keyword.isEmpty()){
            return userRepository.findAll(pageable);
        }

        return userRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContaining(
                        keyword,
                        keyword,
                        keyword,
                        pageable
                );
    }

    // ADMIN - UPDATE USER
    public void updateUser(Long id, RegisterRequest request){

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user.setUpdateTime(LocalDateTime.now());

        userRepository.save(user);
    }

    // ADMIN - DEACTIVATE USER
    public void deactivateUser(Long id){

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus("INACTIVE");

        userRepository.save(user);
    }

    public User validateAdmin(String token){

        if(token == null){
            throw new RuntimeException("Missing token");
        }

        Long userId = JwtUtil.getUserIdFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(!"ADMIN".equals(user.getRole())){
            throw new RuntimeException("Admin access required");
        }

        if(!"ACTIVE".equals(user.getStatus())){
            throw new RuntimeException("Admin account inactive");
        }

        return user;
    }

}