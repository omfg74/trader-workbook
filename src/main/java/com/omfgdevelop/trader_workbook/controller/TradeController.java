package com.omfgdevelop.trader_workbook.controller;

import com.omfgdevelop.trader_workbook.dto.TradeCreateRequest;
import com.omfgdevelop.trader_workbook.dto.TradeResponse;
import com.omfgdevelop.trader_workbook.dto.TradeUpdateRequest;
import com.omfgdevelop.trader_workbook.entity.TradeStatus;
import com.omfgdevelop.trader_workbook.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping
    public Page<TradeResponse> listTrades(
            @RequestParam(defaultValue = "false") boolean all,
            @RequestParam(required = false) TradeStatus status,
            @RequestParam(required = false) Long securityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return tradeService.listTrades(all, status, securityId, dateFrom, dateTo, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse createTrade(@Valid @RequestBody TradeCreateRequest request) {
        return tradeService.createTrade(request);
    }

    @GetMapping("/{id}")
    public TradeResponse getTrade(@PathVariable Long id) {
        return tradeService.getTrade(id);
    }

    @PatchMapping("/{id}")
    public TradeResponse updateTrade(@PathVariable Long id, @Valid @RequestBody TradeUpdateRequest request) {
        return tradeService.updateTrade(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrade(@PathVariable Long id) {
        tradeService.deleteTrade(id);
    }
}
