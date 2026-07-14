package com.trademind.watchlist.entity;

import com.trademind.stock.entity.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "watchlist_items", uniqueConstraints = @UniqueConstraint(columnNames = {"watchlist_id", "stock_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WatchlistItem {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watchlist_id", nullable = false)
    private Watchlist watchlist;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "sort_order")
    @Builder.Default
    private int sortOrder = 0;

    @Column(name = "added_at", updatable = false)
    private Instant addedAt;

    @PrePersist
    protected void onCreate() {
        this.addedAt = Instant.now();
    }
}
