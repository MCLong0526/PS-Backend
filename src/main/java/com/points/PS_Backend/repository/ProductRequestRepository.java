package com.points.PS_Backend.repository;

import com.points.PS_Backend.model.ProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRequestRepository extends JpaRepository<ProductRequest, Long> {

    Page<ProductRequest> findByUserId(Long userId, Pageable pageable);

}