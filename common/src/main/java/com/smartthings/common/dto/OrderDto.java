package com.smartthings.common.dto;

import com.smartthings.common.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDto(
        Long id,
        Long userId,
        String customerName,
        String customerEmail,
        String deliveryAddress,
        String notes,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        List<OrderItemSnapshotDto> items
) {
}

