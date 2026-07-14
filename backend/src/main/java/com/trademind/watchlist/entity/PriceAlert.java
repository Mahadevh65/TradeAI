package com.trademind.watchlist.entity;

import com.trademind.auth.entity.User;
import com.trademind.stock.entity.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "price_alerts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceAlert {

    public enum Condition { ABOVE, BELOW }

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    private Condition condition;

    @Column(name = "target_price")
    private BigDecimal targetPrice;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "triggered_at")
    private Instant triggeredAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
