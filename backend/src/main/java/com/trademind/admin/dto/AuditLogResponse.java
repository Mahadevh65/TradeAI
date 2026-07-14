package com.trademind.admin.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLogResponse {
    private UUID id;
    private String userEmail;
    private String action;
    private String details;
    private String ipAddress;
    private Instant createdAt;
}
