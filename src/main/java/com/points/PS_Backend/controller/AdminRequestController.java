package com.points.PS_Backend.controller;

import com.points.PS_Backend.dto.ApiResponse;
import com.points.PS_Backend.model.Product;
import com.points.PS_Backend.model.Request;
import com.points.PS_Backend.repository.ProductRepository;
import com.points.PS_Backend.repository.RequestRepository;
import com.points.PS_Backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/requests")
public class AdminRequestController {

    private final RequestRepository requestRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public AdminRequestController(RequestRepository requestRepository,
                                  ProductRepository productRepository,
                                  UserService userService) {
        this.requestRepository = requestRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    private String extractToken(HttpServletRequest request){
        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer ")){
            throw new RuntimeException("Missing token");
        }

        return header.substring(7);
    }

    // =========================
    // VERIFY PAYMENT
    // =========================
    @PutMapping("/{id}/verify")
    public ApiResponse verifyPayment(
            @PathVariable Long id,
            HttpServletRequest request){

        String token = extractToken(request);
        userService.validateAdmin(token);

        Request req = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if(!"BANK_TRANSFER".equals(req.getPaymentMethod())){
            throw new RuntimeException("Verify only allowed for bank transfer");
        }

        if(!"PENDING".equals(req.getPaymentStatus())){
            throw new RuntimeException("Payment already processed");
        }

        req.setPaymentStatus("PAID");
        req.setUpdateTime(LocalDateTime.now());

        requestRepository.save(req);

        return new ApiResponse(
                200,
                "Payment verified",
                req,
                null
        );
    }

    // =========================
    // REJECT PAYMENT
    // =========================
    @PutMapping("/{id}/reject")
    public ApiResponse rejectPayment(
            @PathVariable Long id,
            HttpServletRequest request){

        String token = extractToken(request);
        userService.validateAdmin(token);

        Request req = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if(!"BANK_TRANSFER".equals(req.getPaymentMethod())){
            throw new RuntimeException("Reject only allowed for bank transfer");
        }

        if("REJECTED".equals(req.getPaymentStatus())){
            throw new RuntimeException("Payment already rejected");
        }

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // restore stock
        product.setQuantity(product.getQuantity() + 1);

        req.setPaymentStatus("REJECTED");
        req.setStatus("CANCELLED");
        req.setUpdateTime(LocalDateTime.now());

        productRepository.save(product);
        requestRepository.save(req);

        return new ApiResponse(
                200,
                "Payment rejected and stock restored",
                req,
                null
        );
    }

    // =========================
    // SHIP PRODUCT
    // =========================
    @PutMapping("/{id}/ship")
    public ApiResponse shipProduct(
            @PathVariable Long id,
            @RequestParam String trackingNumber,
            HttpServletRequest request){

        String token = extractToken(request);
        userService.validateAdmin(token);

        Request req = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if(!"PAID".equals(req.getPaymentStatus()) && !"COMPLETED".equals(req.getPaymentStatus())){
            throw new RuntimeException("Payment not verified");
        }

        if("SHIPPED".equals(req.getStatus())){
            throw new RuntimeException("Already shipped");
        }

        req.setStatus("SHIPPED");
        req.setTrackingNumber(trackingNumber); // ✅ ADD THIS
        req.setUpdateTime(LocalDateTime.now());

        requestRepository.save(req);

        return new ApiResponse(
                200,
                "Product shipped",
                req,
                null
        );
    }
}