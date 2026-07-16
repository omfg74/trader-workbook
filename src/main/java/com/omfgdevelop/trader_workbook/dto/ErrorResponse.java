package com.omfgdevelop.trader_workbook.dto;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp
) {
}
