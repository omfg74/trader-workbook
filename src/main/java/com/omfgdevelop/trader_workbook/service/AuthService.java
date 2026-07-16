package com.omfgdevelop.trader_workbook.service;

import com.omfgdevelop.trader_workbook.dto.LoginRequest;
import com.omfgdevelop.trader_workbook.dto.RefreshRequest;
import com.omfgdevelop.trader_workbook.dto.RegisterRequest;
import com.omfgdevelop.trader_workbook.dto.TokenResponse;
import com.omfgdevelop.trader_workbook.dto.UserResponse;
import com.omfgdevelop.trader_workbook.entity.Role;
import com.omfgdevelop.trader_workbook.entity.User;
import com.omfgdevelop.trader_workbook.exception.BadRequestException;
import com.omfgdevelop.trader_workbook.repository.UserRepository;
import com.omfgdevelop.trader_workbook.security.AppUserDetails;
import com.omfgdevelop.trader_workbook.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        userRepository.save(user);

        return toResponse(user);
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        return new TokenResponse(
                jwtService.generateAccessToken(request.username()),
                jwtService.generateRefreshToken(request.username())
        );
    }

    public TokenResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();

        if (!jwtService.isTokenValid(token) || !"refresh".equals(jwtService.extractTokenType(token))) {
            throw new BadRequestException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(token);
        return new TokenResponse(
                jwtService.generateAccessToken(username),
                jwtService.generateRefreshToken(username)
        );
    }

    public UserResponse getCurrentUser() {
        return toResponse(getCurrentUserEntity());
    }

    public User getCurrentUserEntity() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("User not found");
        }

        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof AppUserDetails details) {
            username = details.getUsername();
        } else if (principal instanceof String value) {
            username = value;
        } else {
            throw new BadRequestException("User not found");
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.getCreatedAt());
    }
}
