package com.trademind.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AiQueryRequest {
    @NotBlank
    private String question;

    // Optional — if the question references a specific stock, passing the symbol
    // lets the copilot ground its answer in real cached price/fundamentals data.
    private String symbol;
}
