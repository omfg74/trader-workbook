package com.omfgdevelop.trader_workbook.dto;

import com.omfgdevelop.trader_workbook.entity.Side;
import com.omfgdevelop.trader_workbook.entity.TradeStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeCreateRequest(
        @NotNull Long securityId,
        Side side,
        @NotNull @PositiveOrZero BigDecimal purchasePrice,
        @NotNull @PositiveOrZero BigDecimal exchangeCommission,
        @NotNull @PositiveOrZero BigDecimal brokerCommission,
        String comment,
        Instant purchaseDate
) {
}
