package com.trademind.stock.controller;

import com.trademind.auth.dto.ApiResponse;
import com.trademind.stock.dto.HistoricalPricePoint;
import com.trademind.stock.dto.StockDetailsResponse;
import com.trademind.stock.dto.StockSummaryResponse;
import com.trademind.stock.service.StockService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
@Tag(name = "Stocks", description = "Stock details, search, historical data and market movers")
public class StockController {

    private final StockService stockService;

    @GetMapping("/{symbol}")
    public ApiResponse<StockDetailsResponse> getDetails(@PathVariable String symbol) {
        return ApiResponse.success("OK", stockService.getDetails(symbol));
    }

    @GetMapping("/{symbol}/history")
    public ApiResponse<List<HistoricalPricePoint>> getHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "90") int days) {
        return ApiResponse.success("OK", stockService.getHistoricalData(symbol, Math.min(days, 365)));
    }

    @GetMapping("/search")
    public ApiResponse<List<StockSummaryResponse>> search(@RequestParam String q) {
        return ApiResponse.success("OK", stockService.search(q));
    }

    @GetMapping("/movers/gainers")
    public ApiResponse<List<StockSummaryResponse>> gainers() {
        return ApiResponse.success("OK", stockService.topGainers());
    }

    @GetMapping("/movers/losers")
    public ApiResponse<List<StockSummaryResponse>> losers() {
        return ApiResponse.success("OK", stockService.topLosers());
    }

    @PostMapping("/sync")
    public ApiResponse<String> syncStocks() {
        stockService.syncStocksFromMarket();
        return ApiResponse.success("Stocks synchronized successfully", null);
    }
}
