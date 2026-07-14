package com.trademind.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateUserRolesRequest {
    @NotEmpty
    private Set<String> roles; // ADMIN, TRADER, ANALYST
}
