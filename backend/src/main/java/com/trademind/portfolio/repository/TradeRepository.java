package com.trademind.portfolio.repository;

import com.trademind.portfolio.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    Page<Trade> findByUserIdOrderByExecutedAtDesc(UUID userId, Pageable pageable);
    List<Trade> findByUserIdOrderByExecutedAtDesc(UUID userId);
    List<Trade> findByUserIdAndStockIdOrderByExecutedAtAsc(UUID userId, UUID stockId);
}
