package com.trademind.watchlist.controller;

import com.trademind.auth.dto.ApiResponse;
import com.trademind.auth.entity.User;
import com.trademind.watchlist.dto.*;
import com.trademind.watchlist.service.WatchlistService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/watchlists")
@RequiredArgsConstructor
@Tag(name = "Watchlists", description = "Multiple watchlists, items, sorting and price alerts")
@PreAuthorize("hasAnyRole('TRADER','ANALYST','ADMIN')")
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping
    public ApiResponse<WatchlistResponse> create(@AuthenticationPrincipal User user,
                                                  @Valid @RequestBody CreateWatchlistRequest request) {
        return ApiResponse.success("Watchlist created", watchlistService.createWatchlist(user, request.getName()));
    }

    @GetMapping
    public ApiResponse<List<WatchlistResponse>> getAll(@AuthenticationPrincipal User user) {
        return ApiResponse.success("OK", watchlistService.getAll(user.getId()));
    }

    @DeleteMapping("/{watchlistId}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID watchlistId) {
        watchlistService.deleteWatchlist(user.getId(), watchlistId);
        return ApiResponse.success("Watchlist deleted", null);
    }

    @PostMapping("/{watchlistId}/items")
    public ApiResponse<WatchlistResponse> addItem(@AuthenticationPrincipal User user,
                                                   @PathVariable UUID watchlistId,
                                                   @Valid @RequestBody AddWatchlistItemRequest request) {
        return ApiResponse.success("Added to watchlist",
                watchlistService.addItem(user.getId(), watchlistId, request.getSymbol()));
    }

    @DeleteMapping("/{watchlistId}/items/{stockId}")
    public ApiResponse<Void> removeItem(@AuthenticationPrincipal User user,
                                         @PathVariable UUID watchlistId,
                                         @PathVariable UUID stockId) {
        watchlistService.removeItem(user.getId(), watchlistId, stockId);
        return ApiResponse.success("Removed from watchlist", null);
    }

    @GetMapping("/{watchlistId}/items")
    public ApiResponse<List<WatchlistItemResponse>> getItems(
            @AuthenticationPrincipal User user,
            @PathVariable UUID watchlistId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        return ApiResponse.success("OK",
                watchlistService.getItemsSorted(user.getId(), watchlistId, sortBy, direction));
    }

    // ------------------------------------------------------------
    // PRICE ALERTS
    // ------------------------------------------------------------
    @PostMapping("/alerts")
    public ApiResponse<PriceAlertResponse> createAlert(@AuthenticationPrincipal User user,
                                                         @Valid @RequestBody PriceAlertRequest request) {
        return ApiResponse.success("Alert created", watchlistService.createAlert(user, request));
    }

    @GetMapping("/alerts")
    public ApiResponse<List<PriceAlertResponse>> getAlerts(@AuthenticationPrincipal User user) {
        return ApiResponse.success("OK", watchlistService.getAlerts(user.getId()));
    }

    @DeleteMapping("/alerts/{alertId}")
    public ApiResponse<Void> deleteAlert(@AuthenticationPrincipal User user, @PathVariable UUID alertId) {
        watchlistService.deleteAlert(user.getId(), alertId);
        return ApiResponse.success("Alert deleted", null);
    }
}
