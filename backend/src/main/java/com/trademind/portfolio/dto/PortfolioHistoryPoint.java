package com.trademind.portfolio.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioHistoryPoint {
    private LocalDate date;
    private BigDecimal totalValue;
    private BigDecimal totalInvested;
}
