package com.omfgdevelop.trader_workbook.service;

import com.omfgdevelop.trader_workbook.dto.TradeCreateRequest;
import com.omfgdevelop.trader_workbook.dto.TradeResponse;
import com.omfgdevelop.trader_workbook.dto.TradeUpdateRequest;
import com.omfgdevelop.trader_workbook.entity.Role;
import com.omfgdevelop.trader_workbook.entity.Security;
import com.omfgdevelop.trader_workbook.entity.Side;
import com.omfgdevelop.trader_workbook.entity.Trade;
import com.omfgdevelop.trader_workbook.entity.TradeStatus;
import com.omfgdevelop.trader_workbook.entity.User;
import com.omfgdevelop.trader_workbook.exception.ForbiddenException;
import com.omfgdevelop.trader_workbook.exception.NotFoundException;
import com.omfgdevelop.trader_workbook.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final ReferenceService referenceService;
    private final AuthService authService;

    public Page<TradeResponse> listTrades(
            boolean all,
            TradeStatus status,
            Long securityId,
            Instant dateFrom,
            Instant dateTo,
            Pageable pageable
    ) {
        User currentUser = authService.getCurrentUserEntity();
        Long userId = (all && currentUser.getRole() == Role.ADMIN) ? null : currentUser.getId();

        return tradeRepository.findFiltered(userId, status, securityId, dateFrom, dateTo, pageable)
                .map(this::toResponse);
    }

    public TradeResponse getTrade(Long id) {
        Trade trade = getTradeOrThrow(id);
        checkReadAccess(trade);
        return toResponse(trade);
    }

    @Transactional
    public TradeResponse createTrade(TradeCreateRequest request) {
        User currentUser = authService.getCurrentUserEntity();
        Security security = referenceService.getSecurityOrThrow(request.securityId());

        Trade trade = new Trade();
        trade.setUser(currentUser);
        trade.setSecurity(security);
        trade.setSide(request.side() != null ? request.side() : Side.BUY);
        trade.setPurchasePrice(request.purchasePrice());
        trade.setExchangeCommission(request.exchangeCommission());
        trade.setBrokerCommission(request.brokerCommission());
        trade.setComment(request.comment());
        trade.setPurchaseDate(request.purchaseDate() != null ? request.purchaseDate() : Instant.now());
        trade.setStatus(TradeStatus.OPEN);

        return toResponse(tradeRepository.save(trade));
    }

    @Transactional
    public TradeResponse updateTrade(Long id, TradeUpdateRequest request) {
        Trade trade = getTradeOrThrow(id);
        checkWriteAccess(trade);

        if (request.securityId() != null) {
            trade.setSecurity(referenceService.getSecurityOrThrow(request.securityId()));
        }
        if (request.side() != null) {
            trade.setSide(request.side());
        }
        if (request.purchasePrice() != null) {
            trade.setPurchasePrice(request.purchasePrice());
        }
        if (request.exchangeCommission() != null) {
            trade.setExchangeCommission(request.exchangeCommission());
        }
        if (request.brokerCommission() != null) {
            trade.setBrokerCommission(request.brokerCommission());
        }
        if (request.comment() != null) {
            trade.setComment(request.comment());
        }
        if (request.purchaseDate() != null) {
            trade.setPurchaseDate(request.purchaseDate());
        }
        if (Boolean.TRUE.equals(request.clearSale())) {
            trade.setSellPrice(null);
            trade.setSellDate(null);
        } else {
            if (request.sellPrice() != null) {
                trade.setSellPrice(request.sellPrice());
            }
            if (request.sellDate() != null) {
                trade.setSellDate(request.sellDate());
            }
        }

        updateStatus(trade);

        return toResponse(tradeRepository.save(trade));
    }

    @Transactional
    public void deleteTrade(Long id) {
        Trade trade = getTradeOrThrow(id);
        User currentUser = authService.getCurrentUserEntity();

        if (!trade.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Cannot delete another user's trade");
        }

        tradeRepository.delete(trade);
    }

    private Trade getTradeOrThrow(Long id) {
        return tradeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trade not found"));
    }

    private void checkReadAccess(Trade trade) {
        User currentUser = authService.getCurrentUserEntity();
        if (!trade.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Access denied");
        }
    }

    private void checkWriteAccess(Trade trade) {
        User currentUser = authService.getCurrentUserEntity();
        if (!trade.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Cannot modify another user's trade");
        }
    }

    private void updateStatus(Trade trade) {
        if (trade.getSellPrice() != null && trade.getSellDate() != null) {
            trade.setStatus(TradeStatus.CLOSED);
        } else {
            trade.setStatus(TradeStatus.OPEN);
        }
    }

    private TradeResponse toResponse(Trade trade) {
        BigDecimal pnl = null;
        if (trade.getStatus() == TradeStatus.CLOSED && trade.getSellPrice() != null) {
            pnl = trade.getSellPrice()
                    .subtract(trade.getPurchasePrice())
                    .subtract(trade.getExchangeCommission())
                    .subtract(trade.getBrokerCommission());
        }

        return new TradeResponse(
                trade.getId(),
                trade.getUser().getId(),
                trade.getUser().getUsername(),
                trade.getSecurity().getId(),
                trade.getSecurity().getTicker(),
                trade.getSecurity().getName(),
                trade.getSide(),
                trade.getPurchasePrice(),
                trade.getExchangeCommission(),
                trade.getBrokerCommission(),
                trade.getComment(),
                trade.getPurchaseDate(),
                trade.getSellPrice(),
                trade.getSellDate(),
                trade.getStatus(),
                pnl,
                trade.getCreatedAt(),
                trade.getUpdatedAt()
        );
    }
}
