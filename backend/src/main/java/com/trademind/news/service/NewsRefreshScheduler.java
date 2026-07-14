package com.trademind.news.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewsRefreshScheduler {

    private final NewsService newsService;

    // Refresh top-level market headlines every 15 minutes so the news feed
    // and dashboard "Market News" widget stay current without manual refresh.
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void refresh() {
        newsService.refreshMarketNews("stock market");
    }
}
