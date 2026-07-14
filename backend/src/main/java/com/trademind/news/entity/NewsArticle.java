package com.trademind.news.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "news_articles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NewsArticle {

    public enum Sentiment { BULLISH, BEARISH, NEUTRAL }

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 500)
    private String title;

    private String source;

    @Column(nullable = false, unique = true, length = 500)
    private String url;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    private String category;

    @Enumerated(EnumType.STRING)
    private Sentiment sentiment;

    @Column(name = "ai_summary", columnDefinition = "text")
    private String aiSummary;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "fetched_at", updatable = false)
    private Instant fetchedAt;

    @PrePersist
    protected void onCreate() {
        this.fetchedAt = Instant.now();
    }
}
