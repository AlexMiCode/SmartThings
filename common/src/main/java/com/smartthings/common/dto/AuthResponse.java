package com.smartthings.common.dto;

public record AuthResponse(
        String token,
        UserDto user
) {
}

