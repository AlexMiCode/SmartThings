package com.smartthings.common.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductDto(
        Long id,
        String name,
        String description,
        String category,
        String brand,
        BigDecimal price,
        String currency,
        int stockQuantity,
        String imageUrl,
        boolean featured,
        Instant createdAt,
        Instant updatedAt
) {
}

