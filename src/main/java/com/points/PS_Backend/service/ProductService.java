package com.points.PS_Backend.service;

import com.points.PS_Backend.model.Product;
import com.points.PS_Backend.model.Request;
import com.points.PS_Backend.model.User;
import com.points.PS_Backend.repository.ProductImageRepository;
import com.points.PS_Backend.repository.ProductRepository;
import com.points.PS_Backend.repository.RequestRepository;
import com.points.PS_Backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    public ProductService(ProductRepository productRepository,
                          RequestRepository requestRepository,
                          UserRepository userRepository,
                          ProductImageRepository productImageRepository,
                          WalletService walletService) {

        this.productRepository = productRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.productImageRepository = productImageRepository;
        this.walletService = walletService;
    }

    public Page<Product> getProducts(int page,int size){

        Page<Product> products = productRepository.findByStatus(
                "ACTIVE",
                PageRequest.of(page,size)
        );

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

}