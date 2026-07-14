package com.trademind.portfolio.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioSummaryResponse {
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalInvested;
    private BigDecimal totalProfitLoss;
    private BigDecimal totalRoiPercent;
    private BigDecimal todayProfitLoss;
    private BigDecimal weeklyProfitLoss;
    private BigDecimal monthlyProfitLoss;
    private List<HoldingResponse> holdings;
}
