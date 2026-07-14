package com.trademind.news.repository;

import com.trademind.news.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, UUID> {
    List<NewsArticle> findTop50ByOrderByPublishedAtDesc();
    List<NewsArticle> findTop50ByCategoryIgnoreCaseOrderByPublishedAtDesc(String category);
    Optional<NewsArticle> findByUrl(String url);
}
