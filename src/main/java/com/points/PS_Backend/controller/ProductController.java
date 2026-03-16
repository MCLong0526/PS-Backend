package com.points.PS_Backend.controller;

import com.points.PS_Backend.dto.ApiResponse;
import com.points.PS_Backend.model.Product;
import com.points.PS_Backend.model.Request;
import com.points.PS_Backend.repository.RequestRepository;
import com.points.PS_Backend.service.ProductService;
import com.points.PS_Backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final RequestRepository requestRepository;

    public ProductController(ProductService productService,
                             RequestRepository requestRepository) {
        this.productService = productService;
        this.requestRepository = requestRepository;
    }

    private String extractToken(HttpServletRequest request){

        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer ")){
            throw new RuntimeException("Missing token");
        }

        return header.substring(7);
    }

    // LIST PRODUCTS
    @GetMapping
    public ApiResponse getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        var products = productService.getProducts(page,size);

        return new ApiResponse(
                200,
                "success",
                products,
                null
        );
    }

    // MY REQUESTS
    @GetMapping("/my-requests")
    public ApiResponse myRequests(
            HttpServletRequest request,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size){

        String token = extractToken(request);

        Long userId = JwtUtil.getUserIdFromToken(token);

        var requests = productService.getMyRequests(userId,page,size);

        return new ApiResponse(
                200,
                "success",
                requests,
                null
        );
    }

    // CONFIRM RECEIVED
    @PutMapping("/complete/{id}")
    public ApiResponse completeRequest(
            @PathVariable Long id,
            HttpServletRequest request){

        String token = extractToken(request);

        Long userId = JwtUtil.getUserIdFromToken(token);

        productService.completeRequest(userId,id);

        return new ApiResponse(
                200,
                "Order completed",
                null,
                null
        );
    }


    @PostMapping("/redeem")
    public ApiResponse redeemProduct(
            @RequestParam Long productId,
            @RequestParam String redeemType,
            @RequestParam Long addressId,
            @RequestParam(required = false) MultipartFile receipt,
            HttpServletRequest request) throws IOException {

        String token = extractToken(request);
        Long userId = JwtUtil.getUserIdFromToken(token);

        Request req = productService.redeemProduct(
                userId,
                productId,
                redeemType,
                addressId,
                receipt
        );

        return new ApiResponse(
                200,
                "Redeem request created",
                req,
                null
        );
    }


    @GetMapping("/{id}")
    public ApiResponse getProductById(@PathVariable Long id){

        Product product = productService.getProductById(id);

        return new ApiResponse(
                200,
                "success",
                product,
                null
        );
    }

    @PostMapping("/requests/{id}/receipt")
    public ApiResponse uploadReceipt(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {

        String token = extractToken(request);

        Long userId = JwtUtil.getUserIdFromToken(token);

        Request req = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if(!req.getUserId().equals(userId)){
            throw new RuntimeException("Access denied");
        }

        if(!"BANK_TRANSFER".equals(req.getPaymentMethod())){
            throw new RuntimeException("Receipt only allowed for bank transfer");
        }

        if("COMPLETED".equals(req.getPaymentStatus())){
            throw new RuntimeException("Payment already completed");
        }

        if("PAID".equals(req.getPaymentStatus())){
            throw new RuntimeException("Receipt already uploaded");
        }

        if(file.isEmpty()){
            throw new RuntimeException("File cannot be empty");
        }

        String originalName = file.getOriginalFilename();

        if(originalName == null){
            throw new RuntimeException("Invalid file");
        }

        String uploadDir = System.getProperty("user.dir") + "/uploads/receipts/";

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + originalName;

        File dest = new File(uploadDir + fileName);
        file.transferTo(dest);

        String receiptUrl = "/uploads/receipts/" + fileName;

        req.setReceiptUrl(receiptUrl);
        req.setPaymentStatus("PAID");
        req.setUpdateTime(LocalDateTime.now());

        requestRepository.save(req);

        return new ApiResponse(
                200,
                "Receipt uploaded",
                req,
                null
        );
    }

}