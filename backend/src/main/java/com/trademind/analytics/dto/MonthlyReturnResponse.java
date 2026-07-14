package com.trademind.analytics.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MonthlyReturnResponse {
    private String month;   // e.g. "2026-06"
    private java.math.BigDecimal startValue;
    private java.math.BigDecimal endValue;
    private java.math.BigDecimal returnPercent;
}
