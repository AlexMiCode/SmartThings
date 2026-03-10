package com.smartthings.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank String customerName,
        @NotBlank String customerEmail,
        @NotBlank String deliveryAddress,
        String notes,
        @Valid @NotEmpty List<OrderItemRequest> items
) {
}

