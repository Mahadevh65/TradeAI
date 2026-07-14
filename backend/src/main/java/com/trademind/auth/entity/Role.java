package com.trademind.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    private String description;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum RoleName {
        ADMIN, TRADER, ANALYST
    }
}
