package com.trademind.news.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NewsArticleResponse {
    private UUID id;
    private String title;
    private String source;
    private String url;
    private String imageUrl;
    private String category;
    private String sentiment;
    private String aiSummary;
    private Instant publishedAt;
}
