package com.trademind.watchlist.entity;

import com.trademind.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "watchlists", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Watchlist {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "is_default")
    @Builder.Default
    private boolean isDefault = false;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
