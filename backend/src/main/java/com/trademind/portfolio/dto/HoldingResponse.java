package com.trademind.portfolio.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HoldingResponse {
    private UUID holdingId;
    private String symbol;
    private String companyName;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal investedValue;
    private BigDecimal profitLoss;
    private BigDecimal roiPercent;
}
