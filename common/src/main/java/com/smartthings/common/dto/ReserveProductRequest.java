package com.smartthings.common.dto;

import jakarta.validation.constraints.Min;

public record ReserveProductRequest(
        @Min(1) int quantity
) {
}

