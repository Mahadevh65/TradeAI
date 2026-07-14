package com.trademind.portfolio.service;

import com.trademind.auth.entity.User;
import com.trademind.common.exception.BusinessException;
import com.trademind.portfolio.dto.*;
import com.trademind.portfolio.entity.Holding;
import com.trademind.portfolio.entity.PortfolioSnapshot;
import com.trademind.portfolio.entity.Trade;
import com.trademind.portfolio.repository.HoldingRepository;
import com.trademind.portfolio.repository.PortfolioSnapshotRepository;
import com.trademind.portfolio.repository.TradeRepository;
import com.trademind.stock.entity.Stock;
import com.trademind.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;
    private final TradeRepository tradeRepository;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final StockService stockService;

    private static final int SCALE = 4;

    // ------------------------------------------------------------
    // BUY
    // ------------------------------------------------------------
    @Transactional
    public TradeResponse buy(User user, TradeRequest request) {
        Stock stock = stockService.getStockBySymbolOrThrow(request.getSymbol());
        BigDecimal price = resolvePrice(request, stock);
        BigDecimal quantity = request.getQuantity();
        BigDecimal totalAmount = price.multiply(quantity);

        Holding holding = holdingRepository.findByUserIdAndStockId(user.getId(), stock.getId())
                .orElseGet(() -> Holding.builder()
                        .user(user).stock(stock)
                        .quantity(BigDecimal.ZERO).averagePrice(BigDecimal.ZERO)
                        .build());

        BigDecimal existingQty = holding.getQuantity();
        BigDecimal existingCost = existingQty.multiply(holding.getAveragePrice());
        BigDecimal newQty = existingQty.add(quantity);
        BigDecimal newAveragePrice = existingCost.add(totalAmount)
                .divide(newQty, SCALE, RoundingMode.HALF_UP);

        holding.setQuantity(newQty);
        holding.setAveragePrice(newAveragePrice);
        holdingRepository.save(holding);

        Trade trade = Trade.builder()
                .user(user).stock(stock)
                .tradeType(Trade.TradeType.BUY)
                .quantity(quantity).price(price).totalAmount(totalAmount)
                .build();
        trade = tradeRepository.save(trade);

        recordSnapshotForUser(user.getId());
        return toTradeResponse(trade);
    }

    // ------------------------------------------------------------
    // SELL
    // ------------------------------------------------------------
    @Transactional
    public TradeResponse sell(User user, TradeRequest request) {
        Stock stock = stockService.getStockBySymbolOrThrow(request.getSymbol());
        BigDecimal price = resolvePrice(request, stock);
        BigDecimal quantity = request.getQuantity();

        Holding holding = holdingRepository.findByUserIdAndStockId(user.getId(), stock.getId())
                .orElseThrow(() -> new BusinessException(
                        "You don't hold any shares of " + stock.getSymbol(), HttpStatus.BAD_REQUEST));

        if (holding.getQuantity().compareTo(quantity) < 0) {
            throw new BusinessException(
                    "Cannot sell " + quantity + " shares — you only hold " + holding.getQuantity(),
                    HttpStatus.BAD_REQUEST);
        }

        BigDecimal totalAmount = price.multiply(quantity);
        BigDecimal realizedPl = price.subtract(holding.getAveragePrice()).multiply(quantity)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal remainingQty = holding.getQuantity().subtract(quantity);
        holding.setQuantity(remainingQty);
        if (remainingQty.compareTo(BigDecimal.ZERO) == 0) {
            holding.setAveragePrice(BigDecimal.ZERO);
        }
        holdingRepository.save(holding);

        Trade trade = Trade.builder()
                .user(user).stock(stock)
                .tradeType(Trade.TradeType.SELL)
                .quantity(quantity).price(price).totalAmount(totalAmount)
                .realizedPl(realizedPl)
                .build();
        trade = tradeRepository.save(trade);

        recordSnapshotForUser(user.getId());
        return toTradeResponse(trade);
    }

    // ------------------------------------------------------------
    // PORTFOLIO SUMMARY
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getSummary(UUID userId) {
        List<Holding> holdings = holdingRepository.findByUserId(userId).stream()
                .filter(h -> h.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        List<HoldingResponse> holdingResponses = holdings.stream()
                .map(this::toHoldingResponse)
                .collect(Collectors.toList());

        BigDecimal totalValue = holdingResponses.stream()
                .map(HoldingResponse::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInvested = holdingResponses.stream()
                .map(HoldingResponse::getInvestedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPl = totalValue.subtract(totalInvested);
        BigDecimal totalRoi = totalInvested.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalPl.divide(totalInvested, SCALE, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return PortfolioSummaryResponse.builder()
                .totalPortfolioValue(totalValue)
                .totalInvested(totalInvested)
                .totalProfitLoss(totalPl)
                .totalRoiPercent(totalRoi)
                .todayProfitLoss(changeSince(userId, totalValue, LocalDate.now().minusDays(1)))
                .weeklyProfitLoss(changeSince(userId, totalValue, LocalDate.now().minusDays(7)))
                .monthlyProfitLoss(changeSince(userId, totalValue, LocalDate.now().minusDays(30)))
                .holdings(holdingResponses)
                .build();
    }

    private BigDecimal changeSince(UUID userId, BigDecimal currentValue, LocalDate date) {
        return snapshotRepository.findFirstByUserIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(userId, date)
                .map(snap -> currentValue.subtract(snap.getTotalValue()))
                .orElse(BigDecimal.ZERO);
    }

    // ------------------------------------------------------------
    // TRADE HISTORY
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<TradeResponse> getTradeHistory(UUID userId, Pageable pageable) {
        return tradeRepository.findByUserIdOrderByExecutedAtDesc(userId, pageable)
                .map(this::toTradeResponse);
    }

    // ------------------------------------------------------------
    // PORTFOLIO VALUE HISTORY (for the performance line chart)
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<PortfolioHistoryPoint> getHistory(UUID userId, int days) {
        LocalDate from = LocalDate.now().minusDays(days);
        LocalDate to = LocalDate.now();
        return snapshotRepository.findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(userId, from, to)
                .stream()
                .map(s -> PortfolioHistoryPoint.builder()
                        .date(s.getSnapshotDate())
                        .totalValue(s.getTotalValue())
                        .totalInvested(s.getTotalInvested())
                        .build())
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------
    // SNAPSHOTS (called after every trade, and nightly via scheduler)
    // ------------------------------------------------------------
    @Transactional
    public void recordSnapshotForUser(UUID userId) {
        PortfolioSummaryResponse summary = getSummary(userId);
        LocalDate today = LocalDate.now();

        PortfolioSnapshot snapshot = snapshotRepository.findByUserIdAndSnapshotDate(userId, today)
                .orElseGet(() -> PortfolioSnapshot.builder()
                        .user(holdingRepository.findByUserId(userId).stream()
                                .findFirst().map(Holding::getUser).orElse(null))
                        .snapshotDate(today)
                        .build());

        if (snapshot.getUser() == null) return; // no holdings yet, nothing meaningful to snapshot

        snapshot.setTotalValue(summary.getTotalPortfolioValue());
        snapshot.setTotalInvested(summary.getTotalInvested());
        snapshotRepository.save(snapshot);
    }

    // ------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------
    private BigDecimal resolvePrice(TradeRequest request, Stock stock) {
        if (request.getPrice() != null) return request.getPrice();
        if (stock.getLastPrice() != null) return stock.getLastPrice();
        throw new BusinessException(
                "No market price available for " + stock.getSymbol() + " — please supply a price",
                HttpStatus.BAD_REQUEST);
    }

    private HoldingResponse toHoldingResponse(Holding h) {
        BigDecimal currentPrice = h.getStock().getLastPrice() != null
                ? h.getStock().getLastPrice() : h.getAveragePrice();
        BigDecimal currentValue = currentPrice.multiply(h.getQuantity());
        BigDecimal investedValue = h.getAveragePrice().multiply(h.getQuantity());
        BigDecimal pl = currentValue.subtract(investedValue);
        BigDecimal roi = investedValue.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : pl.divide(investedValue, SCALE, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return HoldingResponse.builder()
                .holdingId(h.getId())
                .symbol(h.getStock().getSymbol())
                .companyName(h.getStock().getCompanyName())
                .quantity(h.getQuantity())
                .averagePrice(h.getAveragePrice())
                .currentPrice(currentPrice)
                .currentValue(currentValue)
                .investedValue(investedValue)
                .profitLoss(pl)
                .roiPercent(roi)
                .build();
    }

    private TradeResponse toTradeResponse(Trade t) {
        return TradeResponse.builder()
                .id(t.getId())
                .symbol(t.getStock().getSymbol())
                .tradeType(t.getTradeType().name())
                .quantity(t.getQuantity())
                .price(t.getPrice())
                .totalAmount(t.getTotalAmount())
                .realizedPl(t.getRealizedPl())
                .executedAt(t.getExecutedAt())
                .build();
    }
}
