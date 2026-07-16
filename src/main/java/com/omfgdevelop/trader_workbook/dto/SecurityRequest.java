package com.omfgdevelop.trader_workbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SecurityRequest(
        @NotBlank String ticker,
        @NotBlank String name,
        @NotNull Long typeId,
        Long issuerId,
        String description
) {
}
