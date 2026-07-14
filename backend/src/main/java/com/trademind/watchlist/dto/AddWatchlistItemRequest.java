package com.trademind.watchlist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AddWatchlistItemRequest {
    @NotBlank
    private String symbol;
}
