package com.trademind.common.client;

import com.trademind.config.MarketDataProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsClient {

    public record Article(String title, String source, String url, String imageUrl, Instant publishedAt) {}

    private final WebClient.Builder webClientBuilder;
    private final MarketDataProperties properties;

    @SuppressWarnings("unchecked")
    public Flux<Article> fetchMarketHeadlines(String query) {
        if (properties.getNewsApi().getApiKey() == null || properties.getNewsApi().getApiKey().isBlank()) {
            return Flux.empty();
        }

        WebClient client = webClientBuilder.baseUrl(properties.getNewsApi().getBaseUrl()).build();

        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/everything")
                        .queryParam("q", query)
                        .queryParam("language", "en")
                        .queryParam("sortBy", "publishedAt")
                        .queryParam("pageSize", 30)
                        .queryParam("apiKey", properties.getNewsApi().getApiKey())
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(8))
                .flatMapMany(body -> {
                    List<Map<String, Object>> articles = (List<Map<String, Object>>) body.get("articles");
                    if (articles == null) return Flux.empty();
                    return Flux.fromIterable(articles).map(this::toArticle);
                })
                .onErrorResume(ex -> {
                    log.warn("NewsAPI fetch failed: {}", ex.getMessage());
                    return Flux.empty();
                });
    }

    @SuppressWarnings("unchecked")
    private Article toArticle(Map<String, Object> raw) {
        Map<String, Object> sourceMap = (Map<String, Object>) raw.get("source");
        String sourceName = sourceMap != null ? (String) sourceMap.get("name") : null;
        Instant publishedAt = null;
        Object publishedAtRaw = raw.get("publishedAt");
        if (publishedAtRaw != null) {
            try {
                publishedAt = Instant.parse(publishedAtRaw.toString());
            } catch (Exception ignored) { }
        }
        return new Article(
                (String) raw.get("title"),
                sourceName,
                (String) raw.get("url"),
                (String) raw.get("urlToImage"),
                publishedAt
        );
    }
}
