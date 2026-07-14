package com.trademind.portfolio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TradeRequest {
    @NotBlank
    private String symbol;

    @NotNull
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    // Optional: if omitted, the trade executes at the stock's current cached price
    private BigDecimal price;
}
