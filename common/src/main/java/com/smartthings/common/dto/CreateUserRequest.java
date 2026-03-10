package com.smartthings.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @Size(min = 6, max = 120) String password,
        String role
) {
}

