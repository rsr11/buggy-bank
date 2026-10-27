package com.buggybank.buggy_bank.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buggybank.buggy_bank.auth.dto.LoginRequest;
import com.buggybank.buggy_bank.auth.dto.RegisterRequest;
import com.buggybank.buggy_bank.auth.dto.TokenResponse;
import com.buggybank.buggy_bank.common.exception.ConflictException;
import com.buggybank.buggy_bank.common.exception.UnauthorizedException;
import com.buggybank.buggy_bank.user.UserRepository;
import com.buggybank.buggy_bank.user.dto.UserResponse;
import com.buggybank.buggy_bank.user.entity.User;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }
        User user = new User(email, passwordEncoder.encode(request.password()), request.displayName());
        return UserResponse.from(users.save(user));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = users.findByEmail(email)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        return tokenService.issue(user);
    }
}