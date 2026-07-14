package com.trademind.portfolio.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TradeResponse {
    private UUID id;
    private String symbol;
    private String tradeType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private BigDecimal realizedPl;
    private Instant executedAt;
}
