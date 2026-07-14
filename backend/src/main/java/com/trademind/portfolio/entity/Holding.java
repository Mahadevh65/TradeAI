package com.trademind.portfolio.entity;

import com.trademind.auth.entity.User;
import com.trademind.stock.entity.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "holdings", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "stock_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Holding {

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

    @Builder.Default
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "average_price")
    @Builder.Default
    private BigDecimal averagePrice = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
