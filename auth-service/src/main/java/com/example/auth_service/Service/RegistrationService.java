package com.example.auth_service.Service;

import com.example.auth_service.Dto.RegisterRequest;
import com.example.auth_service.Entity.User;
import com.example.auth_service.Enums.Role;
import com.example.auth_service.Repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.auth_service.Exception.DuplicateResourceException;

import java.util.Locale;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterRequest request) {

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(Role.USER);

        userRepository.save(user);
    }
}
