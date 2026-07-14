package com.trademind.admin.dto;

import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarketConfigResponse {
    private UUID id;
    private String marketName;
    private boolean open;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String timezone;
}
