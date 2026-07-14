package com.trademind.stock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    private String exchange;
    private String sector;
    private String industry;

    @Builder.Default
    private String currency = "USD";

    @Column(name = "country")
    private String country;

    @Column(name = "stock_type")
    private String type;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "last_price")
    private BigDecimal lastPrice;

    @Column(name = "day_change_pct")
    private BigDecimal dayChangePct;

    @Column(name = "market_cap")
    private BigDecimal marketCap;

    @Column(name = "pe_ratio")
    private BigDecimal peRatio;

    private BigDecimal eps;

    @Column(name = "week52_high")
    private BigDecimal week52High;

    @Column(name = "week52_low")
    private BigDecimal week52Low;

    @Column(name = "dividend_yield")
    private BigDecimal dividendYield;

    private Long volume;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

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
