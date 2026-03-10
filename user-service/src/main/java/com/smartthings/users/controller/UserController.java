package com.smartthings.users.controller;

import com.smartthings.common.dto.CreateUserRequest;
import com.smartthings.common.dto.UpdateUserRequest;
import com.smartthings.common.dto.UserDto;
import com.smartthings.users.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public List<UserDto> findAll() {
        return userManagementService.findAll();
    }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Long id) {
        return userManagementService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody CreateUserRequest request) {
        return userManagementService.create(request);
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return userManagementService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userManagementService.delete(id);
    }
}

