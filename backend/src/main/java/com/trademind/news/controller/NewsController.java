package com.trademind.news.controller;

import com.trademind.auth.dto.ApiResponse;
import com.trademind.news.dto.NewsArticleResponse;
import com.trademind.news.service.NewsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Tag(name = "Market News", description = "Latest news with category filters and sentiment tags")
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ApiResponse<List<NewsArticleResponse>> getLatest(@RequestParam(required = false) String category) {
        return ApiResponse.success("OK", newsService.getLatest(category));
    }

    @PostMapping("/refresh")
    public ApiResponse<Void> refresh(@RequestParam(defaultValue = "stock market") String query) {
        newsService.refreshMarketNews(query);
        return ApiResponse.success("News refreshed", null);
    }
}
