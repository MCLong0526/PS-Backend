package com.points.PS_Backend.repository;

import com.points.PS_Backend.model.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Page<Request> findByUserId(Long userId, Pageable pageable);

    @Query("""
    SELECT r FROM Request r
    WHERE
    (:status IS NULL OR r.status = :status)
    AND (:keyword IS NULL OR CAST(r.id AS string) LIKE %:keyword%)
    AND (:startDate IS NULL OR r.createTime >= :startDate)
    AND (:endDate IS NULL OR r.createTime <= :endDate)
    ORDER BY r.createTime DESC, r.id DESC
    """)
    Page<Request> searchRequests(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    boolean existsByUserIdAndProductIdAndStatus(Long userId, Long productId, String status);


    @Query("""
    SELECT r, u.username, u.email, a.receiverName, a.phone, a.addressLine1, a.addressLine2,
           a.city, a.state, a.postcode, a.country
    FROM Request r
    LEFT JOIN User u ON r.userId = u.id
    LEFT JOIN UserAddress a ON r.addressId = a.id
    WHERE
    (:status IS NULL OR r.status = :status)
    AND (:keyword IS NULL OR CAST(r.id AS string) LIKE %:keyword%)
    AND (:startDate IS NULL OR r.createTime >= :startDate)
    AND (:endDate IS NULL OR r.createTime <= :endDate)
    ORDER BY r.createTime DESC
    """)
    Page<Object[]> searchRequestsWithUser(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}