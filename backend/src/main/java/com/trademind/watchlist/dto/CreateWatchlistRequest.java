package com.trademind.watchlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateWatchlistRequest {
    @NotBlank
    @Size(max = 100)
    private String name;
}
