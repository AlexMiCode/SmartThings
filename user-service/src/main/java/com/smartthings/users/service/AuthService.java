package com.smartthings.users.service;

import com.smartthings.common.dto.AuthRequest;
import com.smartthings.common.dto.AuthResponse;
import com.smartthings.common.dto.RegisterRequest;
import com.smartthings.common.dto.UserDto;
import com.smartthings.common.exception.BusinessException;
import com.smartthings.common.security.JwtService;
import com.smartthings.users.entity.AppUser;
import com.smartthings.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("User with email " + request.email() + " already exists");
        }

        AppUser user = new AppUser();
        user.setFullName(request.fullName());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("CUSTOMER");

        AppUser saved = userRepository.save(user);
        log.info("Registered new user with id={}", saved.getId());
        return new AuthResponse(generateToken(saved), toDto(saved));
    }

    public AuthResponse login(AuthRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("Invalid email or password");
        }

        log.info("User {} logged in", user.getEmail());
        return new AuthResponse(generateToken(user), toDto(user));
    }

    private String generateToken(AppUser user) {
        return jwtService.generateToken(
                user.getEmail(),
                Map.of(
                        "userId", String.valueOf(user.getId()),
                        "email", user.getEmail(),
                        "role", user.getRole()
                )
        );
    }

    private UserDto toDto(AppUser user) {
        return new UserDto(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}

