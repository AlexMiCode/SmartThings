package com.smartthings.common.dto;

import java.math.BigDecimal;

public record UpdateProductRequest(
        String name,
        String description,
        String category,
        String brand,
        BigDecimal price,
        Integer stockQuantity,
        String imageUrl,
        Boolean featured
) {
}

