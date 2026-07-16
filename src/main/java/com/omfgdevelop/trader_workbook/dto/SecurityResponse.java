package com.omfgdevelop.trader_workbook.dto;

public record SecurityResponse(
        Long id,
        String ticker,
        String name,
        Long typeId,
        String typeName,
        Long issuerId,
        String issuerName,
        String description
) {
}
