package com.trademind.admin.controller;

import com.trademind.admin.dto.*;
import com.trademind.admin.service.AdminService;
import com.trademind.auth.dto.ApiResponse;
import com.trademind.auth.entity.User;
import com.trademind.portfolio.dto.TradeResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "User/role management, platform-wide trades, audit logs, stats, market config")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ApiResponse<Page<UserManagementResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("OK", adminService.listUsers(PageRequest.of(page, size)));
    }

    @PutMapping("/users/{userId}/roles")
    public ApiResponse<UserManagementResponse> updateRoles(@PathVariable UUID userId,
                                                            @Valid @RequestBody UpdateUserRolesRequest request) {
        return ApiResponse.success("Roles updated", adminService.updateUserRoles(userId, request.getRoles()));
    }

    @PutMapping("/users/{userId}/active")
    public ApiResponse<UserManagementResponse> setActive(@PathVariable UUID userId, @RequestParam boolean active) {
        return ApiResponse.success(active ? "User activated" : "User deactivated",
                adminService.setActive(userId, active));
    }

    @PutMapping("/users/{userId}/unlock")
    public ApiResponse<UserManagementResponse> unlock(@PathVariable UUID userId) {
        return ApiResponse.success("User unlocked", adminService.unlock(userId));
    }

    @GetMapping("/trades")
    public ApiResponse<Page<TradeResponse>> allTrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("OK", adminService.listAllTrades(PageRequest.of(page, size)));
    }

    @GetMapping("/audit-logs")
    public ApiResponse<Page<AuditLogResponse>> auditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.success("OK", adminService.listAuditLogs(action, PageRequest.of(page, size)));
    }

    @GetMapping("/dashboard-stats")
    public ApiResponse<DashboardStatsResponse> dashboardStats() {
        return ApiResponse.success("OK", adminService.getDashboardStats());
    }

    @GetMapping("/market-config")
    public ApiResponse<List<MarketConfigResponse>> marketConfig() {
        return ApiResponse.success("OK", adminService.listMarketConfig());
    }

    @PutMapping("/market-config/{marketName}")
    public ApiResponse<MarketConfigResponse> updateMarketConfig(
            @AuthenticationPrincipal User admin,
            @PathVariable String marketName,
            @RequestBody UpdateMarketConfigRequest request) {
        return ApiResponse.success("Market config updated",
                adminService.updateMarketConfig(marketName, request, admin.getEmail()));
    }
}
