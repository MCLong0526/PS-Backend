package com.points.PS_Backend.controller;

import com.points.PS_Backend.dto.ApiResponse;
import com.points.PS_Backend.dto.CreateLuckyDrawRequest;
import com.points.PS_Backend.dto.RequestResponse;
import com.points.PS_Backend.model.Product;
import com.points.PS_Backend.model.ProductImage;
import com.points.PS_Backend.model.Request;
import com.points.PS_Backend.model.LuckyDrawItem;
import com.points.PS_Backend.repository.*;
import com.points.PS_Backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final RequestRepository requestRepository;
    private final ProductImageRepository productImageRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final LuckyDrawItemRepository luckyDrawItemRepository;

    public AdminProductController(ProductRepository productRepository,
                                  RequestRepository requestRepository,
                                  ProductImageRepository productImageRepository,
                                  UserService userService,
                                  UserRepository userRepository,
                                  UserAddressRepository userAddressRepository,
                                  LuckyDrawItemRepository luckyDrawItemRepository) {

        this.productRepository = productRepository;
        this.requestRepository = requestRepository;
        this.productImageRepository = productImageRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.userAddressRepository = userAddressRepository;
        this.luckyDrawItemRepository = luckyDrawItemRepository;
    }

    private String extractToken(HttpServletRequest request){

        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer ")){
            throw new RuntimeException("Missing token");
        }

        return header.substring(7);
    }

//    // CREATE PRODUCT
//    @PostMapping
//    public ApiResponse createProduct(
//            @RequestBody Product product,
//            HttpServletRequest request){
//
//        String token = extractToken(request);
//
//        userService.validateAdmin(token);
//
//        // ✅ validate product type
//        if(product.getProductType() == null ||
//                (!"NORMAL".equals(product.getProductType()) &&
//                        !"LUCKY_DRAW".equals(product.getProductType()))){
//            throw new RuntimeException("Invalid product type");
//        }
//
//        product.setCreateTime(LocalDateTime.now());
//        product.setStatus("ACTIVE");
//
//        productRepository.save(product);
//
//        return new ApiResponse(200,"Product created",null,null);
//    }

    @PostMapping
    @Transactional
    public ApiResponse createProduct(
            @RequestBody CreateLuckyDrawRequest dto,
            HttpServletRequest request){

        String token = extractToken(request);
        userService.validateAdmin(token);

        if(dto.getProductType() == null ||
                (!"NORMAL".equals(dto.getProductType()) &&
                        !"LUCKY_DRAW".equals(dto.getProductType()))){
            throw new RuntimeException("Invalid product type");
        }

        // ================= CREATE PRODUCT =================
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPriceWallet(dto.getPriceWallet());
        product.setPricePoints(dto.getPricePoints());
        product.setQuantity(dto.getQuantity());
        product.setProductType(dto.getProductType());
        product.setStatus("ACTIVE");
        product.setCreateTime(LocalDateTime.now());

        productRepository.save(product);

        // ================= CREATE REWARDS =================
        if("LUCKY_DRAW".equals(dto.getProductType())){

            if(dto.getRewards() == null || dto.getRewards().isEmpty()){
                throw new RuntimeException("Lucky draw must have at least 1 reward");
            }

            for(CreateLuckyDrawRequest.RewardItem r : dto.getRewards()){

                if(r.getWeight() == null || r.getWeight() <= 0){
                    throw new RuntimeException("Weight must be greater than 0");
                }

                Product rewardProduct = productRepository.findById(r.getRewardProductId())
                        .orElseThrow(() -> new RuntimeException("Reward product not found"));

                if("LUCKY_DRAW".equals(rewardProduct.getProductType())){
                    throw new RuntimeException("Cannot use lucky draw as reward");
                }

                LuckyDrawItem item = new LuckyDrawItem();
                item.setLuckyDrawProductId(product.getId());
                item.setRewardProductId(r.getRewardProductId());
                item.setWeight(r.getWeight());
                item.setCreateTime(LocalDateTime.now());

                luckyDrawItemRepository.save(item);
            }
        }

        return new ApiResponse(200, "Product created", product, null);
    }

    // UPDATE PRODUCT
    @PutMapping("/{id}")
    public ApiResponse updateProduct(
            @PathVariable Long id,
            @RequestBody Product product,
            HttpServletRequest request){

        String token = extractToken(request);

        userService.validateAdmin(token);

        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // ✅ validate product type
        if(product.getProductType() == null ||
                (!"NORMAL".equals(product.getProductType()) &&
                        !"LUCKY_DRAW".equals(product.getProductType()))){
            throw new RuntimeException("Invalid product type");
        }

        p.setName(product.getName());
        p.setDescription(product.getDescription());
        p.setPriceWallet(product.getPriceWallet());
        p.setPricePoints(product.getPricePoints());
        p.setQuantity(product.getQuantity());
        p.setProductType(product.getProductType()); // ✅ important
        p.setUpdateTime(LocalDateTime.now());

        productRepository.save(p);

        return new ApiResponse(200,"Product updated",null,null);
    }

    // DELETE PRODUCT
    @DeleteMapping("/{id}")
    public ApiResponse deleteProduct(
            @PathVariable Long id,
            HttpServletRequest request){

        String token = extractToken(request);

        userService.validateAdmin(token);

        productRepository.deleteById(id);

        return new ApiResponse(200,"Product deleted",null,null);
    }

    // LIST ALL REQUESTS
    @GetMapping("/requests")
    public ApiResponse getRequests(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String keyword,
            @RequestParam(required=false) String startDate,
            @RequestParam(required=false) String endDate,
            HttpServletRequest request){

        String token = extractToken(request);

        userService.validateAdmin(token);

        LocalDateTime start = null;
        LocalDateTime end = null;

        if(startDate != null){
            start = LocalDateTime.parse(startDate + "T00:00:00");
        }

        if(endDate != null){
            end = LocalDateTime.parse(endDate + "T23:59:59");
        }

        var pageResult = requestRepository.searchRequestsWithUser(
                status,
                keyword,
                start,
                end,
                PageRequest.of(page, size)
        );

        var result = pageResult.map(row -> {

            Request r = (Request) row[0];

            RequestResponse dto = new RequestResponse();

            dto.setId(r.getId());
            dto.setRequestType(r.getRequestType());
            dto.setRedeemType(r.getRedeemType());
            dto.setStatus(r.getStatus());
            dto.setCreateTime(r.getCreateTime());
            dto.setUpdateTime(r.getUpdateTime());
            dto.setTrackingNumber(r.getTrackingNumber());

            dto.setProductId(r.getProductId());
            dto.setUserId(r.getUserId());

            dto.setAmountPoints(r.getAmountPoints());
            dto.setAmountWallet(r.getAmountWallet());

            dto.setAddressId(r.getAddressId());

            dto.setPaymentMethod(r.getPaymentMethod());
            dto.setPaymentStatus(r.getPaymentStatus());

            dto.setBankName(r.getBankName());
            dto.setBankAccount(r.getBankAccount());
            dto.setBankHolder(r.getBankHolder());

            dto.setReceiptUrl(r.getReceiptUrl());

            dto.setCompletedTime(r.getCompletedTime());

            // load user
            userRepository.findById(r.getUserId()).ifPresent(user -> {
                dto.setUsername(user.getUsername());
                dto.setEmail(user.getEmail());
            });

            // load address
            if(r.getAddressId() != null){
                userAddressRepository.findById(r.getAddressId()).ifPresent(addr -> {
                    dto.setReceiverName(addr.getReceiverName());
                    dto.setPhone(addr.getPhone());
                    dto.setAddressLine1(addr.getAddressLine1());
                    dto.setAddressLine2(addr.getAddressLine2());
                    dto.setCity(addr.getCity());
                    dto.setState(addr.getState());
                    dto.setPostcode(addr.getPostcode());
                    dto.setCountry(addr.getCountry());
                });
            }

            return dto;
        });

        return new ApiResponse(
                200,
                "success",
                result,
                null
        );
    }

    // SHIP PRODUCT
    @PutMapping("/requests/{id}/ship")
    public ApiResponse shipProduct(
            @PathVariable Long id,
            @RequestParam String trackingNumber,
            HttpServletRequest request){

        String token = extractToken(request);

        userService.validateAdmin(token);

        Request r = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        r.setTrackingNumber(trackingNumber);
        r.setStatus("SHIPPED");
        r.setUpdateTime(LocalDateTime.now());

        requestRepository.save(r);

        return new ApiResponse(200,"Product shipped",null,null);
    }

    @PostMapping("/{productId}/images")
    public ApiResponse uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {

        String token = extractToken(request);
        userService.validateAdmin(token);

        productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String uploadDir = System.getProperty("user.dir") + "/uploads/";

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        File dest = new File(uploadDir + fileName);
        file.transferTo(dest);

        String imageUrl = "http://localhost:8080/uploads/" + fileName;

        ProductImage img = new ProductImage();
        img.setProductId(productId);
        img.setImageUrl(imageUrl);
        img.setCreateTime(LocalDateTime.now());

        productImageRepository.save(img);

        return new ApiResponse(200, "Image uploaded", img, null);
    }

    @DeleteMapping("/images/{imageId}")
    public ApiResponse deleteProductImage(
            @PathVariable Long imageId,
            HttpServletRequest request){

        String token = extractToken(request);

        userService.validateAdmin(token);

        ProductImage img = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        // delete file from disk
        try{
            String imageUrl = img.getImageUrl();

            if(imageUrl != null){
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

                String filePath = System.getProperty("user.dir") + "/uploads/" + fileName;

                File file = new File(filePath);

                if(file.exists()){
                    file.delete();
                }
            }
        }catch(Exception ignored){}

        productImageRepository.delete(img);

        return new ApiResponse(
                200,
                "Image deleted",
                null,
                null
        );
    }


    @PostMapping("/{productId}/lucky-draw-items")
    public ApiResponse addLuckyDrawItem(
            @PathVariable Long productId,
            @RequestParam Long rewardProductId,
            @RequestParam Integer weight,
            HttpServletRequest request){

        String token = extractToken(request);
        userService.validateAdmin(token);

        Product luckyDrawProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Lucky draw product not found"));

        if(!"LUCKY_DRAW".equals(luckyDrawProduct.getProductType())){
            throw new RuntimeException("This product is not a lucky draw");
        }

        Product rewardProduct = productRepository.findById(rewardProductId)
                .orElseThrow(() -> new RuntimeException("Reward product not found"));

        if(weight == null || weight <= 0){
            throw new RuntimeException("Weight must be greater than 0");
        }

        boolean exists = luckyDrawItemRepository
                .existsByLuckyDrawProductIdAndRewardProductId(productId, rewardProductId);

        if(exists){
            throw new RuntimeException("Reward product already added into this lucky draw");
        }

        LuckyDrawItem item = new LuckyDrawItem();
        item.setLuckyDrawProductId(productId);
        item.setRewardProductId(rewardProductId);
        item.setWeight(weight);
        item.setCreateTime(LocalDateTime.now());

        luckyDrawItemRepository.save(item);

        return new ApiResponse(200, "Lucky draw item added", item, null);
    }

    @GetMapping("/{productId}/lucky-draw-items")
    public ApiResponse getLuckyDrawItems(
            @PathVariable Long productId,
            HttpServletRequest request){

        String token = extractToken(request);
        userService.validateAdmin(token);

        Product luckyDrawProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Lucky draw product not found"));

        if(!"LUCKY_DRAW".equals(luckyDrawProduct.getProductType())){
            throw new RuntimeException("This product is not a lucky draw");
        }

        var items = luckyDrawItemRepository.findByLuckyDrawProductId(productId);

        return new ApiResponse(200, "success", items, null);
    }


    @PutMapping("/lucky-draw-items/{itemId}")
    public ApiResponse updateLuckyDrawItem(
            @PathVariable Long itemId,
            @RequestParam Integer weight,
            HttpServletRequest request){

        String token = extractToken(request);
        userService.validateAdmin(token);

        LuckyDrawItem item = luckyDrawItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Lucky draw item not found"));

        if(weight == null || weight <= 0){
            throw new RuntimeException("Weight must be greater than 0");
        }

        item.setWeight(weight);
        item.setUpdateTime(LocalDateTime.now());

        luckyDrawItemRepository.save(item);

        return new ApiResponse(200, "Lucky draw item updated", item, null);
    }


    @DeleteMapping("/lucky-draw-items/{itemId}")
    public ApiResponse deleteLuckyDrawItem(
            @PathVariable Long itemId,
            HttpServletRequest request){

        String token = extractToken(request);
        userService.validateAdmin(token);

        LuckyDrawItem item = luckyDrawItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Lucky draw item not found"));

        luckyDrawItemRepository.delete(item);

        return new ApiResponse(200, "Lucky draw item deleted", null, null);
    }

}