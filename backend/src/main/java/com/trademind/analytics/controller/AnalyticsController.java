package com.trademind.analytics.controller;

import com.trademind.analytics.dto.DividendIncomeResponse;
import com.trademind.analytics.dto.MonthlyReturnResponse;
import com.trademind.analytics.dto.SectorAllocationResponse;
import com.trademind.analytics.service.AnalyticsService;
import com.trademind.auth.dto.ApiResponse;
import com.trademind.auth.entity.User;
import com.trademind.portfolio.dto.PortfolioHistoryPoint;
import com.trademind.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Portfolio growth, sector allocation, monthly returns, dividend income")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final PortfolioService portfolioService;

    @GetMapping("/growth")
    public ApiResponse<List<PortfolioHistoryPoint>> growth(@AuthenticationPrincipal User user,
                                                             @RequestParam(defaultValue = "180") int days) {
        return ApiResponse.success("OK", portfolioService.getHistory(user.getId(), days));
    }

    @GetMapping("/sector-allocation")
    public ApiResponse<List<SectorAllocationResponse>> sectorAllocation(@AuthenticationPrincipal User user) {
        return ApiResponse.success("OK", analyticsService.getSectorAllocation(user.getId()));
    }

    @GetMapping("/monthly-returns")
    public ApiResponse<List<MonthlyReturnResponse>> monthlyReturns(@AuthenticationPrincipal User user,
                                                                    @RequestParam(defaultValue = "12") int months) {
        return ApiResponse.success("OK", analyticsService.getMonthlyReturns(user.getId(), months));
    }

    @GetMapping("/dividend-income")
    public ApiResponse<List<DividendIncomeResponse>> dividendIncome(@AuthenticationPrincipal User user) {
        return ApiResponse.success("OK", analyticsService.getDividendIncomeEstimate(user.getId()));
    }
}
