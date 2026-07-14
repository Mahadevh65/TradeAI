package com.trademind.watchlist.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WatchlistItemResponse {
    private UUID itemId;
    private UUID stockId;
    private String symbol;
    private String companyName;
    private BigDecimal lastPrice;
    private BigDecimal dayChangePct;
}
