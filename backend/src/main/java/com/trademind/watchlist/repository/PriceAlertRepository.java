package com.trademind.watchlist.repository;

import com.trademind.watchlist.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, UUID> {
    List<PriceAlert> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<PriceAlert> findByActiveTrue();
}
