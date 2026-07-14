package com.trademind.analytics.service;

import com.trademind.analytics.dto.DividendIncomeResponse;
import com.trademind.analytics.dto.MonthlyReturnResponse;
import com.trademind.analytics.dto.SectorAllocationResponse;
import com.trademind.portfolio.dto.HoldingResponse;
import com.trademind.portfolio.dto.PortfolioSummaryResponse;
import com.trademind.portfolio.entity.PortfolioSnapshot;
import com.trademind.portfolio.repository.PortfolioSnapshotRepository;
import com.trademind.portfolio.service.PortfolioService;
import com.trademind.stock.entity.Stock;
import com.trademind.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PortfolioService portfolioService;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final StockRepository stockRepository;

    private static final int SCALE = 4;

    @Transactional(readOnly = true)
    public List<SectorAllocationResponse> getSectorAllocation(UUID userId) {
        PortfolioSummaryResponse summary = portfolioService.getSummary(userId);
        BigDecimal total = summary.getTotalPortfolioValue();

        Map<String, BigDecimal> bySector = new LinkedHashMap<>();
        for (HoldingResponse h : summary.getHoldings()) {
            Stock stock = stockRepository.findBySymbolIgnoreCase(h.getSymbol()).orElse(null);
            String sector = (stock != null && stock.getSector() != null) ? stock.getSector() : "Unclassified";
            bySector.merge(sector, h.getCurrentValue(), BigDecimal::add);
        }

        return bySector.entrySet().stream()
                .map(e -> SectorAllocationResponse.builder()
                        .sector(e.getKey())
                        .value(e.getValue())
                        .percentOfPortfolio(total.compareTo(BigDecimal.ZERO) == 0
                                ? BigDecimal.ZERO
                                : e.getValue().divide(total, SCALE, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)))
                        .build())
                .sorted(Comparator.comparing(SectorAllocationResponse::getValue).reversed())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MonthlyReturnResponse> getMonthlyReturns(UUID userId, int monthsBack) {
        LocalDate from = LocalDate.now().minusMonths(monthsBack).withDayOfMonth(1);
        LocalDate to = LocalDate.now();

        List<PortfolioSnapshot> snapshots = snapshotRepository
                .findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(userId, from, to);

        Map<YearMonth, List<PortfolioSnapshot>> byMonth = snapshots.stream()
                .collect(Collectors.groupingBy(s -> YearMonth.from(s.getSnapshotDate())));

        List<MonthlyReturnResponse> results = new ArrayList<>();
        byMonth.forEach((month, list) -> {
            list.sort(Comparator.comparing(PortfolioSnapshot::getSnapshotDate));
            BigDecimal startValue = list.get(0).getTotalValue();
            BigDecimal endValue = list.get(list.size() - 1).getTotalValue();
            BigDecimal returnPct = startValue.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : endValue.subtract(startValue).divide(startValue, SCALE, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

            results.add(MonthlyReturnResponse.builder()
                    .month(month.toString())
                    .startValue(startValue).endValue(endValue)
                    .returnPercent(returnPct)
                    .build());
        });

        results.sort(Comparator.comparing(MonthlyReturnResponse::getMonth));
        return results;
    }

    @Transactional(readOnly = true)
    public List<DividendIncomeResponse> getDividendIncomeEstimate(UUID userId) {
        PortfolioSummaryResponse summary = portfolioService.getSummary(userId);

        return summary.getHoldings().stream().map(h -> {
            Stock stock = stockRepository.findBySymbolIgnoreCase(h.getSymbol()).orElse(null);
            BigDecimal yieldPct = (stock != null && stock.getDividendYield() != null)
                    ? stock.getDividendYield() : BigDecimal.ZERO;

            BigDecimal annualIncome = h.getCurrentValue()
                    .multiply(yieldPct).divide(BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP);

            return DividendIncomeResponse.builder()
                    .symbol(h.getSymbol())
                    .estimatedAnnualIncome(annualIncome)
                    .estimatedMonthlyIncome(annualIncome.divide(BigDecimal.valueOf(12), SCALE, RoundingMode.HALF_UP))
                    .note("Estimate based on current dividend yield; actual payouts vary by declaration date.")
                    .build();
        }).collect(Collectors.toList());
    }
}
