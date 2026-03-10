package com.smartthings.common.dto;

import java.time.Instant;

public record NotificationDto(
        Long id,
        Long orderId,
        Long userId,
        String message,
        Instant createdAt
) {
}

