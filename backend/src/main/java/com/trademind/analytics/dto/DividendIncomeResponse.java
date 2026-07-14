package com.trademind.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DividendIncomeResponse {
    private String symbol;
    private BigDecimal estimatedAnnualIncome;
    private BigDecimal estimatedMonthlyIncome;
    private String note;
}
