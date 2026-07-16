package com.omfgdevelop.trader_workbook.dto;

import com.omfgdevelop.trader_workbook.entity.Side;
import com.omfgdevelop.trader_workbook.entity.TradeStatus;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeUpdateRequest(
        Long securityId,
        Side side,
        @PositiveOrZero BigDecimal purchasePrice,
        @PositiveOrZero BigDecimal exchangeCommission,
        @PositiveOrZero BigDecimal brokerCommission,
        String comment,
        Instant purchaseDate,
        @PositiveOrZero BigDecimal sellPrice,
        Instant sellDate,
        Boolean clearSale
) {
}
