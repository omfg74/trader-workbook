package com.omfgdevelop.trader_workbook.service;

import com.omfgdevelop.trader_workbook.dto.UpdateUserRoleRequest;
import com.omfgdevelop.trader_workbook.dto.UserResponse;
import com.omfgdevelop.trader_workbook.entity.User;
import com.omfgdevelop.trader_workbook.exception.NotFoundException;
import com.omfgdevelop.trader_workbook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(authService::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse updateRole(Long userId, UpdateUserRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setRole(request.role());
        return authService.toResponse(userRepository.save(user));
    }
}
