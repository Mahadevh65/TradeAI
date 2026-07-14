package com.trademind.portfolio.controller;

import com.trademind.auth.dto.ApiResponse;
import com.trademind.auth.entity.User;
import com.trademind.portfolio.dto.*;
import com.trademind.portfolio.service.PortfolioService;
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

@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Buy/sell stocks, holdings, P&L, trade history")
@PreAuthorize("hasAnyRole('TRADER','ADMIN')")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping("/buy")
    public ApiResponse<TradeResponse> buy(@AuthenticationPrincipal User user,
                                           @Valid @RequestBody TradeRequest request) {
        return ApiResponse.success("Trade executed", portfolioService.buy(user, request));
    }

    @PostMapping("/sell")
    public ApiResponse<TradeResponse> sell(@AuthenticationPrincipal User user,
                                            @Valid @RequestBody TradeRequest request) {
        return ApiResponse.success("Trade executed", portfolioService.sell(user, request));
    }

    @GetMapping("/summary")
    public ApiResponse<PortfolioSummaryResponse> summary(@AuthenticationPrincipal User user) {
        return ApiResponse.success("OK", portfolioService.getSummary(user.getId()));
    }

    @GetMapping("/trades")
    public ApiResponse<Page<TradeResponse>> trades(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("OK", portfolioService.getTradeHistory(user.getId(), pageable));
    }

    @GetMapping("/history")
    public ApiResponse<List<PortfolioHistoryPoint>> history(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "90") int days) {
        return ApiResponse.success("OK", portfolioService.getHistory(user.getId(), days));
    }
}
