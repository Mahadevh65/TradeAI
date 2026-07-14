package com.trademind.auth.dto;

import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UUID userId;
    private String fullName;
    private String email;
    private Set<String> roles;
}
