package com.smartthings.common.dto;

import java.math.BigDecimal;

public record OrderItemSnapshotDto(
        Long productId,
        String productName,
        int quantity,
        BigDecimal price
) {
}

