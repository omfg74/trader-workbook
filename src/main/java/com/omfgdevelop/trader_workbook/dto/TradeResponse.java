package com.omfgdevelop.trader_workbook.dto;

import com.omfgdevelop.trader_workbook.entity.Side;
import com.omfgdevelop.trader_workbook.entity.TradeStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeResponse(
        Long id,
        Long userId,
        String username,
        Long securityId,
        String securityTicker,
        String securityName,
        Side side,
        BigDecimal purchasePrice,
        BigDecimal exchangeCommission,
        BigDecimal brokerCommission,
        String comment,
        Instant purchaseDate,
        BigDecimal sellPrice,
        Instant sellDate,
        TradeStatus status,
        BigDecimal pnl,
        Instant createdAt,
        Instant updatedAt
) {
}
