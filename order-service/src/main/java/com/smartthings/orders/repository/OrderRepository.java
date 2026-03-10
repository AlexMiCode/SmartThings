package com.smartthings.orders.repository;

import com.smartthings.orders.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "items")
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
}

