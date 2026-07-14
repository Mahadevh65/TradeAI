package com.trademind.watchlist.service;

import com.trademind.auth.entity.User;
import com.trademind.common.exception.BusinessException;
import com.trademind.stock.entity.Stock;
import com.trademind.stock.service.StockService;
import com.trademind.watchlist.dto.*;
import com.trademind.watchlist.entity.PriceAlert;
import com.trademind.watchlist.entity.Watchlist;
import com.trademind.watchlist.entity.WatchlistItem;
import com.trademind.watchlist.repository.PriceAlertRepository;
import com.trademind.watchlist.repository.WatchlistItemRepository;
import com.trademind.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistItemRepository itemRepository;
    private final PriceAlertRepository priceAlertRepository;
    private final StockService stockService;

    @Transactional
    public WatchlistResponse createWatchlist(User user, String name) {
        if (watchlistRepository.existsByUserIdAndNameIgnoreCase(user.getId(), name)) {
            throw new BusinessException("A watchlist named '" + name + "' already exists", HttpStatus.CONFLICT);
        }
        boolean isFirst = watchlistRepository.findByUserId(user.getId()).isEmpty();
        Watchlist watchlist = Watchlist.builder().user(user).name(name).isDefault(isFirst).build();
        watchlist = watchlistRepository.save(watchlist);
        return toResponse(watchlist, List.of());
    }

    @Transactional(readOnly = true)
    public List<WatchlistResponse> getAll(UUID userId) {
        return watchlistRepository.findByUserId(userId).stream()
                .map(w -> toResponse(w, itemRepository.findByWatchlistIdOrderBySortOrderAsc(w.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteWatchlist(UUID userId, UUID watchlistId) {
        Watchlist watchlist = getOwned(userId, watchlistId);
        watchlistRepository.delete(watchlist);
    }

    @Transactional
    public WatchlistResponse addItem(UUID userId, UUID watchlistId, String symbol) {
        Watchlist watchlist = getOwned(userId, watchlistId);
        Stock stock = stockService.getStockBySymbolOrThrow(symbol);

        if (itemRepository.findByWatchlistIdAndStockId(watchlistId, stock.getId()).isEmpty()) {
            int nextOrder = itemRepository.findByWatchlistIdOrderBySortOrderAsc(watchlistId).size();
            WatchlistItem item = WatchlistItem.builder()
                    .watchlist(watchlist).stock(stock).sortOrder(nextOrder).build();
            itemRepository.save(item);
        }
        return toResponse(watchlist, itemRepository.findByWatchlistIdOrderBySortOrderAsc(watchlistId));
    }

    @Transactional
    public void removeItem(UUID userId, UUID watchlistId, UUID stockId) {
        getOwned(userId, watchlistId); // ownership check
        itemRepository.deleteByWatchlistIdAndStockId(watchlistId, stockId);
    }

    @Transactional(readOnly = true)
    public List<WatchlistItemResponse> getItemsSorted(UUID userId, UUID watchlistId, String sortBy, String direction) {
        getOwned(userId, watchlistId);
        List<WatchlistItemResponse> items = itemRepository.findByWatchlistIdOrderBySortOrderAsc(watchlistId)
                .stream().map(this::toItemResponse).collect(Collectors.toList());

        Comparator<WatchlistItemResponse> comparator = switch (sortBy == null ? "" : sortBy) {
            case "price" -> Comparator.comparing(WatchlistItemResponse::getLastPrice,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "change" -> Comparator.comparing(WatchlistItemResponse::getDayChangePct,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "name" -> Comparator.comparing(WatchlistItemResponse::getCompanyName,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(WatchlistItemResponse::getSymbol);
        };
        if ("desc".equalsIgnoreCase(direction)) comparator = comparator.reversed();
        items.sort(comparator);
        return items;
    }

    // ------------------------------------------------------------
    // PRICE ALERTS
    // ------------------------------------------------------------
    @Transactional
    public PriceAlertResponse createAlert(User user, PriceAlertRequest request) {
        Stock stock = stockService.getStockBySymbolOrThrow(request.getSymbol());
        PriceAlert.Condition condition;
        try {
            condition = PriceAlert.Condition.valueOf(request.getCondition().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Condition must be ABOVE or BELOW", HttpStatus.BAD_REQUEST);
        }

        PriceAlert alert = PriceAlert.builder()
                .user(user).stock(stock).condition(condition)
                .targetPrice(request.getTargetPrice()).active(true)
                .build();
        return toAlertResponse(priceAlertRepository.save(alert));
    }

    @Transactional(readOnly = true)
    public List<PriceAlertResponse> getAlerts(UUID userId) {
        return priceAlertRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toAlertResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteAlert(UUID userId, UUID alertId) {
        PriceAlert alert = priceAlertRepository.findById(alertId)
                .orElseThrow(() -> new BusinessException("Alert not found", HttpStatus.NOT_FOUND));
        if (!alert.getUser().getId().equals(userId)) {
            throw new BusinessException("Alert not found", HttpStatus.NOT_FOUND);
        }
        priceAlertRepository.delete(alert);
    }

    // ------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------
    private Watchlist getOwned(UUID userId, UUID watchlistId) {
        return watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new BusinessException("Watchlist not found", HttpStatus.NOT_FOUND));
    }

    private WatchlistResponse toResponse(Watchlist w, List<WatchlistItem> items) {
        return WatchlistResponse.builder()
                .id(w.getId()).name(w.getName()).isDefault(w.isDefault())
                .items(items.stream().map(this::toItemResponse).collect(Collectors.toList()))
                .build();
    }

    private WatchlistItemResponse toItemResponse(WatchlistItem item) {
        Stock s = item.getStock();
        return WatchlistItemResponse.builder()
                .itemId(item.getId()).stockId(s.getId()).symbol(s.getSymbol())
                .companyName(s.getCompanyName()).lastPrice(s.getLastPrice())
                .dayChangePct(s.getDayChangePct())
                .build();
    }

    private PriceAlertResponse toAlertResponse(PriceAlert a) {
        return PriceAlertResponse.builder()
                .id(a.getId()).symbol(a.getStock().getSymbol())
                .condition(a.getCondition().name()).targetPrice(a.getTargetPrice())
                .active(a.isActive()).triggeredAt(a.getTriggeredAt())
                .build();
    }
}
