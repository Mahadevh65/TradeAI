package com.trademind.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8)
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).+$",
        message = "Password must contain uppercase, lowercase, digit and special character"
    )
    private String newPassword;
}
