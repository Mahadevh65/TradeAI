package com.trademind.portfolio.service;

import com.trademind.portfolio.repository.PortfolioSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioSnapshotScheduler {

    private final PortfolioSnapshotRepository snapshotRepository;
    private final PortfolioService portfolioService;

    /**
     * Records a daily snapshot of every user's portfolio value at market close
     * (00:05 server time). This is what powers the performance line chart and
     * today/weekly/monthly P&L figures — without it there'd be nothing to diff
     * against.
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void recordDailySnapshots() {
        var userIds = snapshotRepository.findDistinctUserIdsWithHoldings();
        log.info("Recording daily portfolio snapshots for {} users", userIds.size());
        for (var userId : userIds) {
            try {
                portfolioService.recordSnapshotForUser(userId);
            } catch (Exception e) {
                log.warn("Failed to record snapshot for user {}: {}", userId, e.getMessage());
            }
        }
    }
}
