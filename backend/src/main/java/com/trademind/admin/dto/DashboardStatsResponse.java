package com.trademind.admin.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long lockedUsers;
    private long totalTrades;
    private long tradesToday;
    private long totalWatchlists;
    private long totalStocksCached;
}
