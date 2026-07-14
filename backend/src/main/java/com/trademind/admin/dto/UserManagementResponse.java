package com.trademind.admin.dto;

import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserManagementResponse {
    private UUID id;
    private String fullName;
    private String email;
    private Set<String> roles;
    private boolean emailVerified;
    private boolean active;
    private boolean locked;
    private Instant lastLoginAt;
    private Instant createdAt;
}
