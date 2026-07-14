package com.trademind.watchlist.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WatchlistResponse {
    private UUID id;
    private String name;
    private boolean isDefault;
    private List<WatchlistItemResponse> items;
}
