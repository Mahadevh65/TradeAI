package com.trademind.ai.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AiQueryResponse {
    private String summary;
    private String riskAnalysis;
    private List<String> pros;
    private List<String> cons;
    private String technicalAnalysis;
    private String fundamentalAnalysis;
    private String recommendation;     // BUY, SELL, HOLD, WATCH
    private BigDecimal confidenceScore; // 0-100
}
