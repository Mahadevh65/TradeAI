package com.trademind.portfolio.repository;

import com.trademind.portfolio.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoldingRepository extends JpaRepository<Holding, UUID> {
    List<Holding> findByUserId(UUID userId);
    Optional<Holding> findByUserIdAndStockId(UUID userId, UUID stockId);
}
