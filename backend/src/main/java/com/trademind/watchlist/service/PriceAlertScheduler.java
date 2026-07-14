package com.trademind.watchlist.service;

import com.trademind.common.util.EmailService;
import com.trademind.watchlist.entity.PriceAlert;
import com.trademind.watchlist.repository.PriceAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceAlertScheduler {

    private final PriceAlertRepository priceAlertRepository;
    private final EmailService emailService;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void checkAlerts() {
        var activeAlerts = priceAlertRepository.findByActiveTrue();
        for (PriceAlert alert : activeAlerts) {
            BigDecimal currentPrice = alert.getStock().getLastPrice();
            if (currentPrice == null) continue;

            boolean triggered = alert.getCondition() == PriceAlert.Condition.ABOVE
                    ? currentPrice.compareTo(alert.getTargetPrice()) >= 0
                    : currentPrice.compareTo(alert.getTargetPrice()) <= 0;

            if (triggered) {
                alert.setActive(false);
                alert.setTriggeredAt(Instant.now());
                priceAlertRepository.save(alert);

                emailService.sendPriceAlertEmail(
                        alert.getUser().getEmail(),
                        alert.getStock().getSymbol(),
                        alert.getCondition().name(),
                        alert.getTargetPrice().toString(),
                        currentPrice.toString()
                );
                log.info("Price alert triggered for {} on {}", alert.getUser().getEmail(), alert.getStock().getSymbol());
            }
        }
    }
}
