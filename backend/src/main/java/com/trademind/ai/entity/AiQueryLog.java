package com.trademind.ai.entity;

import com.trademind.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_query_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AiQueryLog {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "text")
    private String question;

    @Column(name = "answer_summary", columnDefinition = "text")
    private String answerSummary;

    private String recommendation;

    @Column(name = "confidence_score")
    private BigDecimal confidenceScore;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
