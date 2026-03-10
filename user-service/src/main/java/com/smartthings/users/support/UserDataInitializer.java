package com.smartthings.users.support;

import com.smartthings.users.entity.AppUser;
import com.smartthings.users.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserDataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmailIgnoreCase("admin@smartthings.local")) {
            AppUser admin = new AppUser();
            admin.setFullName("Store Admin");
            admin.setEmail("admin@smartthings.local");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
    }
}

