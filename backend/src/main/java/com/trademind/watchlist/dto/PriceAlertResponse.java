package com.trademind.watchlist.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceAlertResponse {
    private UUID id;
    private String symbol;
    private String condition;
    private BigDecimal targetPrice;
    private boolean active;
    private Instant triggeredAt;
}
