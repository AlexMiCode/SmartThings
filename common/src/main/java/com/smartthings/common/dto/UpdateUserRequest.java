package com.smartthings.common.dto;

public record UpdateUserRequest(
        String fullName,
        String password,
        String role
) {
}

