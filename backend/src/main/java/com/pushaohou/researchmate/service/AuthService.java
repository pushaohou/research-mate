package com.pushaohou.researchmate.service;

import com.pushaohou.researchmate.dto.RegisterRequest;
import com.pushaohou.researchmate.dto.UserResponse;
import com.pushaohou.researchmate.entity.User;
import com.pushaohou.researchmate.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                request.username(),
                request.email(),
                passwordHash
        );

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }
}