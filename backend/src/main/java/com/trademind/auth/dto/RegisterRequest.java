package com.trademind.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 150)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).+$",
        message = "Password must contain uppercase, lowercase, digit and special character"
    )
    private String password;

    private String phoneNumber;

    // ADMIN role assignment is never allowed via public registration;
    // default role is TRADER unless explicitly requested as ANALYST.
    private String requestedRole;
}
