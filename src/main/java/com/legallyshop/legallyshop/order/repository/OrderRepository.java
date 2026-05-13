package com.legallyshop.legallyshop.order.repository;

import com.legallyshop.legallyshop.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Order> findByOrderCodeAndUserId(String orderCode, Long userId);

    Optional<Order> findByOrderCode(String orderCode);
}
