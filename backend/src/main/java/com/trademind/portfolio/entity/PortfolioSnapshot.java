package com.trademind.portfolio.entity;

import com.trademind.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "portfolio_snapshots", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "snapshot_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioSnapshot {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_value", nullable = false)
    private BigDecimal totalValue;

    @Column(name = "total_invested", nullable = false)
    private BigDecimal totalInvested;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
