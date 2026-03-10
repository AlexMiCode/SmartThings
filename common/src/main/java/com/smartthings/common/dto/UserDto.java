package com.smartthings.common.dto;

import java.time.Instant;

public record UserDto(
        Long id,
        String fullName,
        String email,
        String role,
        Instant createdAt
) {
}

