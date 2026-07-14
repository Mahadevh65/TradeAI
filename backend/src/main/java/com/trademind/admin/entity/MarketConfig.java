package com.trademind.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "market_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarketConfig {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "market_name", nullable = false, unique = true, length = 50)
    private String marketName;

    @Column(name = "is_open")
    private boolean open;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    private String timezone;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
