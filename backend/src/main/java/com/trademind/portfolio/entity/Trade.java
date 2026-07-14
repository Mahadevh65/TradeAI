package com.trademind.portfolio.entity;

import com.trademind.auth.entity.User;
import com.trademind.stock.entity.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Trade {

    public enum TradeType { BUY, SELL }

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
    @Column(name = "trade_type", nullable = false)
    private TradeType tradeType;

    private BigDecimal quantity;
    private BigDecimal price;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "realized_pl")
    private BigDecimal realizedPl;

    @Column(name = "executed_at")
    private Instant executedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        if (this.executedAt == null) this.executedAt = now;
    }
}
