package com.smartthings.users.service;

import com.smartthings.common.dto.CreateUserRequest;
import com.smartthings.common.dto.UpdateUserRequest;
import com.smartthings.common.exception.BusinessException;
import com.smartthings.common.exception.NotFoundException;
import com.smartthings.users.entity.AppUser;
import com.smartthings.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserManagementService userManagementService;

    private AppUser existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new AppUser();
        existingUser.setFullName("Admin User");
        existingUser.setEmail("admin@smartthings.local");
        existingUser.setPasswordHash("hashed");
        existingUser.setRole("ADMIN");
        setIdAndCreatedAt(existingUser, 1L);
    }

    @Test
    void createUsesCustomerRoleByDefault() {
        CreateUserRequest request = new CreateUserRequest("Demo User", "demo@smartthings.local", "secret123", null);
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            setIdAndCreatedAt(user, 2L);
            return user;
        });

        var response = userManagementService.create(request);

        assertThat(response.role()).isEqualTo("CUSTOMER");
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("demo@smartthings.local")).thenReturn(true);

        assertThatThrownBy(() -> userManagementService.create(
                new CreateUserRequest("Demo User", "demo@smartthings.local", "secret123", null)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updateChangesFullNamePasswordAndRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newSecret")).thenReturn("newHash");
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        var response = userManagementService.update(1L, new UpdateUserRequest("Updated User", "newSecret", "manager"));

        assertThat(response.fullName()).isEqualTo("Updated User");
        assertThat(existingUser.getPasswordHash()).isEqualTo("newHash");
        assertThat(existingUser.getRole()).isEqualTo("MANAGER");
    }

    @Test
    void findByIdThrowsWhenUserMissing() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userManagementService.findById(42L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteThrowsWhenUserMissing() {
        when(userRepository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> userManagementService.delete(42L))
                .isInstanceOf(NotFoundException.class);
    }

    private void setIdAndCreatedAt(AppUser user, Long id) {
        try {
            var idField = AppUser.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
            var createdAtField = AppUser.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(user, Instant.now());
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}

