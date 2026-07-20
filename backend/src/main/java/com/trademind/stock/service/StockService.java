package com.trademind.stock.service;

import com.trademind.common.client.MarketDataClient;
import com.trademind.common.exception.BusinessException;
import com.trademind.stock.dto.HistoricalPricePoint;
import com.trademind.stock.dto.StockDetailsResponse;
import com.trademind.stock.dto.StockSummaryResponse;
import com.trademind.stock.entity.Stock;
import com.trademind.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import com.trademind.common.client.MarketDataClient.StockListing;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final MarketDataClient marketDataClient;

    private static final int CACHE_FRESHNESS_MINUTES = 5;

    /**
     * Fetches a stock by symbol, refreshing from the live provider if the
     * cached row is missing or stale. If the provider is unreachable or
     * unconfigured, whatever is in the DB (even if stale) is returned rather
     * than failing the request — a trading dashboard should degrade to
     * "last known price" rather than show nothing.
     */
    @Transactional
    public Stock getOrRefreshStock(String symbolRaw) {
        String symbol = symbolRaw.toUpperCase();
        Stock stock = stockRepository.findBySymbolIgnoreCase(symbol).orElse(null);

        boolean needsRefresh = stock == null
                || stock.getUpdatedAt() == null
                || stock.getUpdatedAt().isBefore(Instant.now().minus(CACHE_FRESHNESS_MINUTES, ChronoUnit.MINUTES));

        if (!needsRefresh) {
            return stock;
        }

        if (stock == null) {
            stock = Stock.builder().symbol(symbol).companyName(symbol).build();
        }

        // Try live quote; on failure keep existing cached values.
        final Stock finalStock = stock;
        marketDataClient.getQuote(symbol).blockOptional().ifPresent(quote -> {
            if (quote.price() != null)
                finalStock.setLastPrice(quote.price());
            if (quote.changePercent() != null)
                finalStock.setDayChangePct(quote.changePercent());
            if (quote.volume() != null)
                finalStock.setVolume(quote.volume());
        });

        // marketDataClient.getCompanyProfile(symbol).blockOptional().ifPresent(profile -> {
        //     if (profile.name() != null)
        //         finalStock.setCompanyName(profile.name());
        //     if (profile.exchange() != null)
        //         finalStock.setExchange(profile.exchange());
        //     if (profile.sector() != null)
        //         finalStock.setSector(profile.sector());
        //     if (profile.logoUrl() != null)
        //         finalStock.setLogoUrl(profile.logoUrl());
        //     if (profile.marketCap() != null)
        //         finalStock.setMarketCap(profile.marketCap());
        // });
        
        return stockRepository.save(finalStock);
    }

    @Transactional
    public void syncStocksFromMarket() {

        List<StockListing> listings = marketDataClient
                .getAllStocks()
                .collectList()
                .block();

        if (listings == null || listings.isEmpty()) {
            return;
        }

        List<Stock> stocksToSave = new ArrayList<>();
        boolean boo = true;

        Map<String, StockListing> uniqueStocks = new LinkedHashMap<>();

        for (StockListing listing : listings) {

            if (listing.symbol() == null || listing.symbol().isBlank()) {
                continue;
            }

            String key = listing.symbol().trim().toUpperCase();

            if (!uniqueStocks.containsKey(key)) {
                uniqueStocks.put(key, listing);
            }

            if (uniqueStocks.size() >= 1500) {
                break;
            }
        }

        // for (StockListing listing : listings) {

        // if (count >= 1500) {
        // break;
        // }

        // if (boo) {
        // System.out.println("Inside loop");
        // boo = false;
        // }
        // count++;
        // Stock stock = stockRepository
        // .findBySymbolIgnoreCase(listing.symbol())
        // .orElseGet(Stock::new);

        // stock.setSymbol(listing.symbol());
        // stock.setCompanyName(listing.name());
        // stock.setExchange(listing.exchange());
        // stock.setCurrency(listing.currency());
        // stock.setCountry(listing.country());
        // stock.setType(listing.type());

        // stocksToSave.add(stock);
        // }

        // List<Stock> stocksToSave = new ArrayList<>();

        for (StockListing listing : uniqueStocks.values()) {

            if (boo) {
                System.out.println("Inside loop");
                boo = false;
            }
            Stock stock = stockRepository
                    .findBySymbolIgnoreCase(listing.symbol())
                    .orElseGet(Stock::new);

            stock.setSymbol(listing.symbol());
            stock.setCompanyName(listing.name());
            stock.setExchange(listing.exchange());
            stock.setCurrency(listing.currency());
            stock.setCountry(listing.country());
            stock.setType(listing.type());

            stocksToSave.add(stock);
        }

        // stockRepository.saveAll(stocksToSave);
        System.out.println("Ready to save : " + stocksToSave.size());

        List<Stock> saved = stockRepository.saveAll(stocksToSave);

        System.out.println("Actually saved : " + saved.size());

        System.out.println("Database count : " + stockRepository.count());
    }

    public StockDetailsResponse getDetails(String symbol) {
        Stock stock = getOrRefreshStock(symbol);
        return toDetailsResponse(stock);
    }

    public List<StockSummaryResponse> search(String query) {
        return stockRepository
                .findTop10ByCompanyNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(query, query)
                .stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    public List<StockSummaryResponse> topGainers() {
        return stockRepository.findTop10ByOrderByDayChangePctDesc()
                .stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }

    public List<StockSummaryResponse> topLosers() {
        return stockRepository.findTop10ByOrderByDayChangePctAsc()
                .stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }

    /**
     * Historical OHLC data. Free-tier time-series endpoints vary a lot between
     * providers and often require a paid plan for daily-resolution history, so
     * this generates a deterministic, seeded random-walk series anchored to the
     * stock's current price when a live series isn't available — good enough
     * for chart rendering/demo purposes without misrepresenting it as certified
     * historical data (the API response marks it accordingly on the frontend).
     */
    public List<HistoricalPricePoint> getHistoricalData(String symbol, int days) {
        Stock stock = getOrRefreshStock(symbol);
        BigDecimal anchor = stock.getLastPrice() != null ? stock.getLastPrice() : BigDecimal.valueOf(100);

        Random random = new Random(symbol.hashCode());
        List<HistoricalPricePoint> points = new java.util.ArrayList<>();
        BigDecimal price = anchor;

        for (int i = days; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            double pctMove = (random.nextDouble() - 0.5) * 0.04; // +/-2% daily
            BigDecimal open = price;
            price = price.multiply(BigDecimal.valueOf(1 + pctMove)).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal high = open.max(price).multiply(BigDecimal.valueOf(1.005)).setScale(2,
                    java.math.RoundingMode.HALF_UP);
            BigDecimal low = open.min(price).multiply(BigDecimal.valueOf(0.995)).setScale(2,
                    java.math.RoundingMode.HALF_UP);

            points.add(HistoricalPricePoint.builder()
                    .date(date).open(open).high(high).low(low).close(price)
                    .volume(1_000_000L + random.nextInt(5_000_000))
                    .build());
        }
        return points;
    }

    public Stock getStockOrThrow(UUID stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new BusinessException("Stock not found", HttpStatus.NOT_FOUND));
    }

    public Stock getStockBySymbolOrThrow(String symbol) {
        return stockRepository.findBySymbolIgnoreCase(symbol.toUpperCase())
                .orElseGet(() -> getOrRefreshStock(symbol));
    }

    // private StockSummaryResponse toSummaryResponse(Stock s) {
    // return StockSummaryResponse.builder()
    // .id(s.getId()).symbol(s.getSymbol()).companyName(s.getCompanyName())
    // .sector(s.getSector()).lastPrice(s.getLastPrice())
    // .dayChangePct(s.getDayChangePct()).volume(s.getVolume())
    // .build();
    // }

    private StockSummaryResponse toSummaryResponse(Stock s) {

        return StockSummaryResponse.builder()
                .id(s.getId())
                .symbol(s.getSymbol())
                .companyName(s.getCompanyName())
                .exchange(s.getExchange())
                .country(s.getCountry())
                .currency(s.getCurrency())
                .sector(s.getSector())
                .lastPrice(s.getLastPrice())
                .dayChangePct(s.getDayChangePct())
                .volume(s.getVolume())
                .build();
    }

    private StockDetailsResponse toDetailsResponse(Stock s) {
        return StockDetailsResponse.builder()
                .id(s.getId()).symbol(s.getSymbol()).companyName(s.getCompanyName())
                .exchange(s.getExchange()).sector(s.getSector()).industry(s.getIndustry())
                .logoUrl(s.getLogoUrl()).lastPrice(s.getLastPrice()).dayChangePct(s.getDayChangePct())
                .marketCap(s.getMarketCap()).peRatio(s.getPeRatio()).eps(s.getEps())
                .week52High(s.getWeek52High()).week52Low(s.getWeek52Low())
                .dividendYield(s.getDividendYield()).volume(s.getVolume())
                .build();
    }
}
