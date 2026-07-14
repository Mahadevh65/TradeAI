package com.trademind.watchlist.repository;

import com.trademind.watchlist.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, UUID> {
    List<WatchlistItem> findByWatchlistIdOrderBySortOrderAsc(UUID watchlistId);
    Optional<WatchlistItem> findByWatchlistIdAndStockId(UUID watchlistId, UUID stockId);
    void deleteByWatchlistIdAndStockId(UUID watchlistId, UUID stockId);
}
