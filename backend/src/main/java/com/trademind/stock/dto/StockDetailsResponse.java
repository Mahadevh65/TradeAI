package com.trademind.stock.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockDetailsResponse {
    private UUID id;
    private String symbol;
    private String companyName;
    private String exchange;
    private String sector;
    private String industry;
    private String logoUrl;
    private BigDecimal lastPrice;
    private BigDecimal dayChangePct;
    private BigDecimal marketCap;
    private BigDecimal peRatio;
    private BigDecimal eps;
    private BigDecimal week52High;
    private BigDecimal week52Low;
    private BigDecimal dividendYield;
    private Long volume;
}
