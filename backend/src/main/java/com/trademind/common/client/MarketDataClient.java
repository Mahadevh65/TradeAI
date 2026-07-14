package com.trademind.common.client;

import com.trademind.config.MarketDataProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Thin client over free-tier market data providers (Twelve Data primary,
 * Finnhub as a fallback/secondary source). Every call is defensive: on any
 * error (rate limit, missing key, network) it returns Mono.empty() rather
 * than throwing, so callers can fall back to cached DB data instead of
 * breaking the request.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MarketDataClient {

    private final WebClient.Builder webClientBuilder;
    private final MarketDataProperties properties;
    private final ObjectMapper mapper = new ObjectMapper();

    public record Quote(
            String symbol,
            BigDecimal price,
            BigDecimal changePercent,
            Long volume) {
    }

    public record CompanyProfile(
            String symbol,
            String name,
            String exchange,
            String sector,
            String industry,
            String logoUrl,
            BigDecimal marketCap,
            BigDecimal peRatio,
            BigDecimal eps,
            BigDecimal week52High,
            BigDecimal week52Low,
            BigDecimal dividendYield) {
    }

    public record StockListing(
            String symbol,
            String name,
            String exchange,
            String currency,
            String country,
            String type) {
    }

    // @SuppressWarnings("unchecked")
    // public Flux<StockListing> getAllStocks() {

    // if (isBlank(properties.getTwelveData().getApiKey())) {
    // return Flux.empty();
    // }

    // WebClient client = webClientBuilder
    // .baseUrl(properties.getTwelveData().getBaseUrl())
    // .build();

    // return client.get()

    // .uri(uriBuilder -> uriBuilder
    // .path("/stocks")
    // .queryParam("apikey", properties.getTwelveData().getApiKey())
    // .build())

    // .retrieve()

    // .bodyToMono(Map.class)

    // .timeout(Duration.ofSeconds(30))

    // .flatMapMany(body -> {

    // List<Map<String, Object>> rows = (List<Map<String, Object>>)
    // body.get("data");

    // if (rows == null) {
    // return Flux.empty();
    // }

    // List<StockListing> stocks = new ArrayList<>();

    // for (Map<String, Object> row : rows) {

    // stocks.add(

    // new StockListing(

    // (String) row.get("symbol"),

    // (String) row.get("name"),

    // (String) row.get("exchange"),

    // (String) row.get("currency"),

    // (String) row.get("country"),

    // (String) row.get("type")

    // )

    // );

    // }

    // return Flux.fromIterable(stocks);

    // })

    // .onErrorResume(ex -> {

    // log.error("Unable to download stock list", ex);

    // return Flux.empty();

    // });

    // }

    @SuppressWarnings("unchecked")
    public Flux<StockListing> getAllStocks() {

        if (isBlank(properties.getTwelveData().getApiKey())) {
            return Flux.empty();
        }

        WebClient client = webClientBuilder
                .baseUrl(properties.getTwelveData().getBaseUrl())
                .build();

        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stocks")
                        .queryParam("apikey", properties.getTwelveData().getApiKey())
                        .build())

                .retrieve()

                .bodyToMono(String.class)

                .timeout(Duration.ofMinutes(3))

                .flatMapMany(json -> {

                    try {

                        JsonNode root = mapper.readTree(json);

                        JsonNode data = root.get("data");

                        if (data == null || !data.isArray()) {
                            return Flux.empty();
                        }

                        List<StockListing> stocks = new ArrayList<>();

                        for (JsonNode node : data) {

                            stocks.add(new StockListing(

                                    node.path("symbol").asText(),

                                    node.path("name").asText(),

                                    node.path("exchange").asText(),

                                    node.path("currency").asText(),

                                    node.path("country").asText(),

                                    node.path("type").asText()

                ));
                        }

                        log.info("Downloaded {} stocks from Twelve Data", stocks.size());

                        return Flux.fromIterable(stocks);

                    } catch (Exception e) {

                        log.error("JSON Parsing Failed", e);

                        return Flux.empty();

                    }

                })

                .onErrorResume(ex -> {

                    log.error("Unable to download stock list", ex);

                    return Flux.empty();

                });

    }

    public Mono<Quote> getQuote(String symbol) {
        if (isBlank(properties.getTwelveData().getApiKey())) {
            return Mono.empty();
        }
        WebClient client = webClientBuilder.baseUrl(properties.getTwelveData().getBaseUrl()).build();

        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/quote")
                        .queryParam("symbol", symbol)
                        .queryParam("apikey", properties.getTwelveData().getApiKey())
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .map(body -> new Quote(
                        symbol,
                        parseDecimal(body.get("close")),
                        parseDecimal(body.get("percent_change")),
                        parseLong(body.get("volume"))))
                .onErrorResume(ex -> {
                    log.warn("Twelve Data quote lookup failed for {}: {}", symbol, ex.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<CompanyProfile> getCompanyProfile(String symbol) {
        if (isBlank(properties.getFinnhub().getApiKey())) {
            return Mono.empty();
        }
        WebClient client = webClientBuilder.baseUrl(properties.getFinnhub().getBaseUrl()).build();

        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/stock/profile2")
                        .queryParam("symbol", symbol)
                        .queryParam("token", properties.getFinnhub().getApiKey())
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .map(body -> new CompanyProfile(
                        symbol,
                        (String) body.get("name"),
                        (String) body.get("exchange"),
                        (String) body.get("finnhubIndustry"),
                        (String) body.get("finnhubIndustry"),
                        (String) body.get("logo"),
                        parseDecimal(body.get("marketCapitalization")),
                        null, null, null, null, null))
                .onErrorResume(ex -> {
                    log.warn("Finnhub profile lookup failed for {}: {}", symbol, ex.getMessage());
                    return Mono.empty();
                });
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private BigDecimal parseDecimal(Object value) {
        if (value == null)
            return null;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(Object value) {
        if (value == null)
            return null;
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
