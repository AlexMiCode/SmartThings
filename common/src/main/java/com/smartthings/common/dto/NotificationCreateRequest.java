package com.smartthings.common.dto;

public record NotificationCreateRequest(
        Long orderId,
        Long userId,
        String message
) {
}

