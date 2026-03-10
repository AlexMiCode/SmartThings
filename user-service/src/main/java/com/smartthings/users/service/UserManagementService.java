package com.smartthings.users.service;

import com.smartthings.common.dto.CreateUserRequest;
import com.smartthings.common.dto.UpdateUserRequest;
import com.smartthings.common.dto.UserDto;
import com.smartthings.common.exception.BusinessException;
import com.smartthings.common.exception.NotFoundException;
import com.smartthings.users.entity.AppUser;
import com.smartthings.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserManagementService {
    private static final Logger log = LoggerFactory.getLogger(UserManagementService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    public UserDto findById(Long id) {
        return toDto(getById(id));
    }

    public UserDto create(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("User with email " + request.email() + " already exists");
        }

        AppUser user = new AppUser();
        user.setFullName(request.fullName());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role() == null || request.role().isBlank() ? "CUSTOMER" : request.role().toUpperCase());
        AppUser saved = userRepository.save(user);
        log.info("Created user with id={}", saved.getId());
        return toDto(saved);
    }

    public UserDto update(Long id, UpdateUserRequest request) {
        AppUser user = getById(id);

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (request.role() != null && !request.role().isBlank()) {
            user.setRole(request.role().toUpperCase());
        }

        AppUser saved = userRepository.save(user);
        log.info("Updated user with id={}", saved.getId());
        return toDto(saved);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
        log.info("Deleted user with id={}", id);
    }

    private AppUser getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }

    private UserDto toDto(AppUser user) {
        return new UserDto(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}

