package com.trademind.news.service;

import com.trademind.common.client.NewsClient;
import com.trademind.news.dto.NewsArticleResponse;
import com.trademind.news.entity.NewsArticle;
import com.trademind.news.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsClient newsClient;
    private final NewsArticleRepository newsArticleRepository;

    private static final Set<String> BULLISH_WORDS = Set.of(
            "surge", "soars", "rally", "jumps", "gains", "record high", "beats estimates",
            "upgraded", "outperform", "breakthrough", "profit rises");
    private static final Set<String> BEARISH_WORDS = Set.of(
            "plunge", "tumbles", "slumps", "falls", "crash", "misses estimates", "downgraded",
            "underperform", "losses", "sell-off", "layoffs", "recall");

    @Transactional
    public void refreshMarketNews(String query) {
        newsClient.fetchMarketHeadlines(query)
                .collectList()
                .blockOptional()
                .ifPresent(articles -> articles.forEach(a -> {
                    if (a.url() == null || newsArticleRepository.findByUrl(a.url()).isPresent()) return;

                    NewsArticle entity = NewsArticle.builder()
                            .title(a.title())
                            .source(a.source())
                            .url(a.url())
                            .imageUrl(a.imageUrl())
                            .category(query)
                            .sentiment(classifySentiment(a.title()))
                            .publishedAt(a.publishedAt())
                            .build();
                    newsArticleRepository.save(entity);
                }));
    }

    @Transactional(readOnly = true)
    public List<NewsArticleResponse> getLatest(String category) {
        List<NewsArticle> articles = (category == null || category.isBlank())
                ? newsArticleRepository.findTop50ByOrderByPublishedAtDesc()
                : newsArticleRepository.findTop50ByCategoryIgnoreCaseOrderByPublishedAtDesc(category);

        return articles.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Simple keyword-based sentiment heuristic. Good enough for a "Bullish /
     * Bearish / Neutral" badge on a news feed; swap for the AI Copilot's
     * classifier (via AiClient) if higher-fidelity sentiment is needed later.
     */
    private NewsArticle.Sentiment classifySentiment(String title) {
        if (title == null) return NewsArticle.Sentiment.NEUTRAL;
        String lower = title.toLowerCase(Locale.ROOT);
        boolean bullish = BULLISH_WORDS.stream().anyMatch(lower::contains);
        boolean bearish = BEARISH_WORDS.stream().anyMatch(lower::contains);
        if (bullish && !bearish) return NewsArticle.Sentiment.BULLISH;
        if (bearish && !bullish) return NewsArticle.Sentiment.BEARISH;
        return NewsArticle.Sentiment.NEUTRAL;
    }

    private NewsArticleResponse toResponse(NewsArticle a) {
        return NewsArticleResponse.builder()
                .id(a.getId()).title(a.getTitle()).source(a.getSource()).url(a.getUrl())
                .imageUrl(a.getImageUrl()).category(a.getCategory())
                .sentiment(a.getSentiment() != null ? a.getSentiment().name() : "NEUTRAL")
                .aiSummary(a.getAiSummary()).publishedAt(a.getPublishedAt())
                .build();
    }
}
