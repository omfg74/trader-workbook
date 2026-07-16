package com.omfgdevelop.trader_workbook.dto;

import com.omfgdevelop.trader_workbook.entity.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        Role role,
        Instant createdAt
) {
}
