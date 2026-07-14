package com.trademind.admin.service;

import com.trademind.admin.dto.*;
import com.trademind.admin.repository.AuditLogRepository;
import com.trademind.admin.repository.MarketConfigRepository;
import com.trademind.auth.entity.Role;
import com.trademind.auth.entity.User;
import com.trademind.auth.repository.RoleRepository;
import com.trademind.auth.repository.UserRepository;
import com.trademind.common.exception.BusinessException;
import com.trademind.portfolio.dto.TradeResponse;
import com.trademind.portfolio.entity.Trade;
import com.trademind.portfolio.repository.TradeRepository;
import com.trademind.stock.repository.StockRepository;
import com.trademind.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TradeRepository tradeRepository;
    private final WatchlistRepository watchlistRepository;
    private final StockRepository stockRepository;
    private final AuditLogRepository auditLogRepository;
    private final MarketConfigRepository marketConfigRepository;

    // ------------------------------------------------------------
    // USERS
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<UserManagementResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserResponse);
    }

    @Transactional
    public UserManagementResponse updateUserRoles(UUID userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name.toUpperCase())
                        .orElseThrow(() -> new BusinessException("Unknown role: " + name, HttpStatus.BAD_REQUEST)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserManagementResponse setActive(UUID userId, boolean active) {
        User user = getUserOrThrow(userId);
        user.setActive(active);
        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserManagementResponse unlock(UUID userId) {
        User user = getUserOrThrow(userId);
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        return toUserResponse(userRepository.save(user));
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
    }

    // ------------------------------------------------------------
    // TRADES (platform-wide view)
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<TradeResponse> listAllTrades(Pageable pageable) {
        return tradeRepository.findAll(pageable).map(this::toTradeResponse);
    }

    // ------------------------------------------------------------
    // AUDIT LOGS
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> listAuditLogs(String action, Pageable pageable) {
        Page<com.trademind.admin.entity.AuditLog> page = (action == null || action.isBlank())
                ? auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                : auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);

        return page.map(log -> AuditLogResponse.builder()
                .id(log.getId())
                .userEmail(log.getUser() != null ? log.getUser().getEmail() : null)
                .action(log.getAction())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build());
    }

    // ------------------------------------------------------------
    // DASHBOARD STATS
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(User::isActive).count();
        long lockedUsers = userRepository.findAll().stream().filter(User::isLocked).count();
        long totalTrades = tradeRepository.count();

        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
        long tradesToday = tradeRepository.findAll().stream()
                .filter(t -> t.getExecutedAt() != null && t.getExecutedAt().isAfter(startOfDay))
                .count();

        long totalWatchlists = watchlistRepository.findAll().size();
        long totalStocksCached = stockRepository.count();

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers).activeUsers(activeUsers).lockedUsers(lockedUsers)
                .totalTrades(totalTrades).tradesToday(tradesToday)
                .totalWatchlists(totalWatchlists).totalStocksCached(totalStocksCached)
                .build();
    }

    // ------------------------------------------------------------
    // MARKET CONFIG
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    public java.util.List<MarketConfigResponse> listMarketConfig() {
        return marketConfigRepository.findAll().stream().map(mc -> MarketConfigResponse.builder()
                .id(mc.getId()).marketName(mc.getMarketName()).open(mc.isOpen())
                .openTime(mc.getOpenTime()).closeTime(mc.getCloseTime()).timezone(mc.getTimezone())
                .build()).collect(Collectors.toList());
    }

    @Transactional
    public MarketConfigResponse updateMarketConfig(String marketName, UpdateMarketConfigRequest request, String updatedBy) {
        var config = marketConfigRepository.findByMarketNameIgnoreCase(marketName)
                .orElseThrow(() -> new BusinessException("Market not found: " + marketName, HttpStatus.NOT_FOUND));

        if (request.getOpen() != null) config.setOpen(request.getOpen());
        if (request.getOpenTime() != null) config.setOpenTime(request.getOpenTime());
        if (request.getCloseTime() != null) config.setCloseTime(request.getCloseTime());
        if (request.getTimezone() != null) config.setTimezone(request.getTimezone());
        config.setUpdatedBy(updatedBy);

        var saved = marketConfigRepository.save(config);
        return MarketConfigResponse.builder()
                .id(saved.getId()).marketName(saved.getMarketName()).open(saved.isOpen())
                .openTime(saved.getOpenTime()).closeTime(saved.getCloseTime()).timezone(saved.getTimezone())
                .build();
    }

    // ------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------
    private UserManagementResponse toUserResponse(User u) {
        return UserManagementResponse.builder()
                .id(u.getId()).fullName(u.getFullName()).email(u.getEmail())
                .roles(u.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .emailVerified(u.isEmailVerified()).active(u.isActive()).locked(u.isLocked())
                .lastLoginAt(u.getLastLoginAt()).createdAt(u.getCreatedAt())
                .build();
    }

    private TradeResponse toTradeResponse(Trade t) {
        return TradeResponse.builder()
                .id(t.getId()).symbol(t.getStock().getSymbol())
                .tradeType(t.getTradeType().name()).quantity(t.getQuantity())
                .price(t.getPrice()).totalAmount(t.getTotalAmount())
                .realizedPl(t.getRealizedPl()).executedAt(t.getExecutedAt())
                .build();
    }
}
