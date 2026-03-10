package com.smartthings.users.service;

import com.smartthings.common.dto.AuthRequest;
import com.smartthings.common.dto.AuthResponse;
import com.smartthings.common.dto.RegisterRequest;
import com.smartthings.common.exception.BusinessException;
import com.smartthings.common.security.JwtService;
import com.smartthings.users.entity.AppUser;
import com.smartthings.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private AppUser existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new AppUser();
        existingUser.setFullName("Demo User");
        existingUser.setEmail("demo@smartthings.local");
        existingUser.setPasswordHash("hashed");
        existingUser.setRole("CUSTOMER");
    }

    @Test
    void registerCreatesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Demo User", "demo@smartthings.local", "secret123");
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed");
        when(jwtService.generateToken(any(), any())).thenReturn("token");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setRole("CUSTOMER");
            user.setEmail(request.email());
            setIdAndCreatedAt(user, 10L);
            return user;
        });

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("token");
        assertThat(response.user().email()).isEqualTo(request.email());

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("Demo User", "demo@smartthings.local", "secret123");
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void loginReturnsTokenWhenPasswordMatches() {
        AuthRequest request = new AuthRequest("demo@smartthings.local", "secret123");
        setIdAndCreatedAt(existingUser, 11L);
        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(request.password(), existingUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(any(), any())).thenReturn("token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("token");
        assertThat(response.user().id()).isEqualTo(11L);
    }

    @Test
    void loginRejectsUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("missing@smartthings.local")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new AuthRequest("missing@smartthings.local", "secret123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void loginRejectsWrongPassword() {
        when(userRepository.findByEmailIgnoreCase(existingUser.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong", existingUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new AuthRequest(existingUser.getEmail(), "wrong")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid email or password");
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
