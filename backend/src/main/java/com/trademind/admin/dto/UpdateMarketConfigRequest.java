package com.trademind.admin.dto;

import lombok.*;

import java.time.LocalTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateMarketConfigRequest {
    private Boolean open;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String timezone;
}
