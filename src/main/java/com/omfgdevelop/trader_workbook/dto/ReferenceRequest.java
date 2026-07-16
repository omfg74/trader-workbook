package com.omfgdevelop.trader_workbook.dto;

import jakarta.validation.constraints.NotBlank;

public record ReferenceRequest(
        @NotBlank String name,
        String description
) {
}
