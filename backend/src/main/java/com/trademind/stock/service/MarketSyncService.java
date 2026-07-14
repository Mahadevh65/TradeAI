package com.trademind.stock.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketSyncService {

    private final StockService stockService;

    /**
     * Runs once when Spring Boot starts.
     */
    // @EventListener(ApplicationReadyEvent.class)
    public void initialSync() {

        log.info("==========================================");
        log.info("Starting Twelve Data Stock Synchronization");
        log.info("==========================================");

        try {

            stockService.syncStocksFromMarket();

            log.info("Stock synchronization completed successfully.");

        } catch (Exception e) {

            log.error("Stock synchronization failed", e);

        }

    }

    /**
     * Refresh every 6 hours.
     */
    // @Scheduled(cron = "0 0 */6 * * *")
    public void scheduledSync() {

        log.info("Running scheduled stock synchronization...");

        try {

            stockService.syncStocksFromMarket();

            log.info("Scheduled synchronization completed.");

        } catch (Exception e) {

            log.error("Scheduled synchronization failed", e);

        }
        
    }

}