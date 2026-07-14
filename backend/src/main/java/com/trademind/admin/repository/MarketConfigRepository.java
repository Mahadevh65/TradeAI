package com.trademind.admin.repository;

import com.trademind.admin.entity.MarketConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketConfigRepository extends JpaRepository<MarketConfig, UUID> {
    List<MarketConfig> findAll();
    Optional<MarketConfig> findByMarketNameIgnoreCase(String marketName);
}
