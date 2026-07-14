package com.trademind.admin.entity;

import com.trademind.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(columnDefinition = "text")
    private String details;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;
}
