package com.trademind.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SectorAllocationResponse {
    private String sector;
    private BigDecimal value;
    private BigDecimal percentOfPortfolio;
}
