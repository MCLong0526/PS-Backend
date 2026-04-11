package com.points.PS_Backend.service;

import com.points.PS_Backend.model.LuckyDrawItem;
import com.points.PS_Backend.model.Product;
import com.points.PS_Backend.model.Request;
import com.points.PS_Backend.model.User;
import com.points.PS_Backend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Random;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final RequestRepository requestRepository;
    private final WalletService walletService;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final LuckyDrawItemRepository luckyDrawItemRepository;

    public ProductService(ProductRepository productRepository,
                          RequestRepository requestRepository,
                          UserRepository userRepository,
                          ProductImageRepository productImageRepository,
                          WalletService walletService,
                          LuckyDrawItemRepository luckyDrawItemRepository) {

        this.productRepository = productRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.productImageRepository = productImageRepository;
        this.walletService = walletService;
        this.luckyDrawItemRepository = luckyDrawItemRepository;
    }

    public Page<Product> getProducts(int page, int size, String productType){

        Page<Product> products;

        if(productType == null || productType.isBlank()){
            products = productRepository.findByStatus(
                    "ACTIVE",
                    PageRequest.of(page, size)
            );
        } else {
            products = productRepository.findByStatusAndProductType(
                    "ACTIVE",
                    productType,
                    PageRequest.of(page, size)
            );
        }

        products.forEach(p -> {
            var images = productImageRepository.findByProductId(p.getId());
            p.setImages(images);
        });

        return products;
    }

    @Transactional
    public Request redeemProduct(Long userId,
                                 Long productId,
                                 String redeemType,
                                 Long addressId,
                                 MultipartFile receipt) throws IOException {

        Product product = productRepository.findProductForUpdate(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean pendingRequest = requestRepository
                .existsByUserIdAndProductIdAndStatus(userId, productId, "PENDING");

        if(pendingRequest){
            throw new RuntimeException("You already have a pending redemption for this product");
        }

        if(product.getQuantity() <= 0){
            throw new RuntimeException("Product out of stock");
        }

        if(addressId == null){
            throw new RuntimeException("Shipping address required");
        }

        if(!"POINT".equals(redeemType)
                && !"WALLET".equals(redeemType)
                && !"BANK_TRANSFER".equals(redeemType)){
            throw new RuntimeException("Invalid redeem type");
        }

        Request request = new Request();

        request.setUserId(userId);
        request.setProductId(productId);
        request.setAddressId(addressId);
        request.setRequestType("PRODUCT_REDEEM");
        request.setRedeemType(redeemType);
        request.setPaymentMethod(redeemType);
        request.setStatus("PENDING");
        request.setCreateTime(LocalDateTime.now());

        // POINT PAYMENT
        if("POINT".equals(redeemType)){

            if(user.getPoints() < product.getPricePoints()){
                throw new RuntimeException("Not enough points");
            }

            user.setPoints(user.getPoints() - product.getPricePoints());

            request.setAmountPoints(product.getPricePoints());
            request.setPaymentStatus("PAID");

            product.setQuantity(product.getQuantity() - 1);
        }

        // WALLET PAYMENT
        else if("WALLET".equals(redeemType)){

            if(user.getWallet().compareTo(product.getPriceWallet()) < 0){
                throw new RuntimeException("Insufficient wallet balance");
            }

            user.setWallet(
                    user.getWallet().subtract(product.getPriceWallet())
            );

            request.setAmountWallet(product.getPriceWallet());
            request.setPaymentStatus("PAID");

            walletService.recordTransaction(
                    userId,
                    product.getPriceWallet().negate(),
                    "PRODUCT_REDEEM",
                    "Redeem product: " + product.getName(),
                    productId
            );

            product.setQuantity(product.getQuantity() - 1);
        }

        // BANK TRANSFER
        else if("BANK_TRANSFER".equals(redeemType)){

            request.setAmountWallet(product.getPriceWallet());
            request.setPaymentStatus("PENDING");

            // reserve stock immediately
            product.setQuantity(product.getQuantity() - 1);

            if(product.getQuantity() < 0){
                throw new RuntimeException("Product out of stock");
            }

            if(receipt == null || receipt.isEmpty()){
                throw new RuntimeException("Receipt required for bank transfer");
            }

            if(!receipt.getContentType().startsWith("image/")){
                throw new RuntimeException("Only image files allowed");
            }

            String originalName = receipt.getOriginalFilename();

            String uploadDir = System.getProperty("user.dir") + "/uploads/receipts/";

            File directory = new File(uploadDir);
            if(!directory.exists()){
                directory.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + originalName;

            File dest = new File(uploadDir + fileName);

            receipt.transferTo(dest);

            String receiptUrl = "/uploads/receipts/" + fileName;

            request.setReceiptUrl(receiptUrl);
        }

        userRepository.save(user);
        productRepository.save(product);

        request = requestRepository.save(request);

        return request;
    }

    public Page<Request> getMyRequests(Long userId,int page,int size){

        return requestRepository.findByUserId(
                userId,
                PageRequest.of(page,size)
        );
    }

    public void completeRequest(Long userId,Long requestId){

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if(!request.getUserId().equals(userId)){
            throw new RuntimeException("Access denied");
        }

        request.setStatus("COMPLETED");
        request.setCompletedTime(LocalDateTime.now());

        requestRepository.save(request);
    }

    public Product getProductById(Long productId){

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        var images = productImageRepository.findByProductId(productId);

        product.setImages(images);

        return product;
    }
    @Transactional
    public Request joinLuckyDraw(Long userId,
                                 Long productId,
                                 String redeemType) {

        Product drawProduct = productRepository.findProductForUpdate(productId)
                .orElseThrow(() -> new RuntimeException("Lucky draw not found"));

        if(!"LUCKY_DRAW".equals(drawProduct.getProductType())){
            throw new RuntimeException("This is not a lucky draw product");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ================= PAYMENT =================

        if("POINT".equals(redeemType)){

            if(user.getPoints() < drawProduct.getPricePoints()){
                throw new RuntimeException("Not enough points");
            }

            user.setPoints(user.getPoints() - drawProduct.getPricePoints());

        } else if("WALLET".equals(redeemType)){

            if(user.getWallet().compareTo(drawProduct.getPriceWallet()) < 0){
                throw new RuntimeException("Insufficient wallet balance");
            }

            user.setWallet(user.getWallet().subtract(drawProduct.getPriceWallet()));

            walletService.recordTransaction(
                    userId,
                    drawProduct.getPriceWallet().negate(),
                    "LUCKY_DRAW",
                    "Join lucky draw: " + drawProduct.getName(),
                    productId
            );

        } else {
            throw new RuntimeException("Lucky draw only supports POINT or WALLET");
        }

        // ================= GET ITEMS =================

        var items = luckyDrawItemRepository.findByLuckyDrawProductId(productId);

        if(items.isEmpty()){
            throw new RuntimeException("Lucky draw has no reward items");
        }

        int totalWeight = items.stream()
                .mapToInt(LuckyDrawItem::getWeight)
                .sum();

        if(totalWeight <= 0){
            throw new RuntimeException("Invalid lucky draw configuration (weight = 0)");
        }

        // ================= PICK REWARD =================

        Random randomGen = new Random();
        Product rewardProduct = null;

        for(int i = 0; i < 5; i++) {

            int random = randomGen.nextInt(totalWeight);
            int cumulative = 0;

            for (LuckyDrawItem item : items) {
                cumulative += item.getWeight();

                if (random < cumulative) {

                    Product p = productRepository
                            .findProductForUpdate(item.getRewardProductId())
                            .orElse(null);

                    if(p != null && p.getQuantity() > 0){
                        rewardProduct = p;
                        break;
                    }
                }
            }

            if(rewardProduct != null) break;
        }

        if(rewardProduct == null){
            throw new RuntimeException("All rewards out of stock");
        }

        // double safety check
        if(rewardProduct.getQuantity() <= 0){
            throw new RuntimeException("Reward out of stock");
        }

        rewardProduct.setQuantity(rewardProduct.getQuantity() - 1);

        // ================= CREATE REQUEST =================

        Request request = new Request();

        request.setUserId(userId);
        request.setProductId(rewardProduct.getId());
        request.setRequestType("LUCKY_DRAW");
        request.setRedeemType(redeemType);
        request.setPaymentMethod(redeemType);

        request.setStatus("PENDING");
        request.setPaymentStatus("PAID");

        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());

        if("POINT".equals(redeemType)){
            request.setAmountPoints(drawProduct.getPricePoints());
        }

        if("WALLET".equals(redeemType)){
            request.setAmountWallet(drawProduct.getPriceWallet());
        }

        request.setSourceProductId(productId);

        // ================= SAVE =================

        userRepository.save(user);
        productRepository.save(rewardProduct);

        return requestRepository.save(request);
    }
}