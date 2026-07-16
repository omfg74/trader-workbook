package com.omfgdevelop.trader_workbook.dto;

import com.omfgdevelop.trader_workbook.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull Role role
) {
}
