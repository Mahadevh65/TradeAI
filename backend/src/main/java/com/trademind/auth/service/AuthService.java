package com.trademind.auth.service;

import com.trademind.auth.dto.*;
import com.trademind.auth.entity.*;
import com.trademind.auth.repository.*;
import com.trademind.common.exception.BusinessException;
import com.trademind.common.util.AuditService;
import com.trademind.common.util.EmailService;
import com.trademind.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuditService auditService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    // ------------------------------------------------------------
    // REGISTER
    // ------------------------------------------------------------
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("An account with this email already exists", HttpStatus.CONFLICT);
        }

        // Public registration can only self-assign TRADER or ANALYST.
        // ADMIN accounts must be created via the admin panel (Module 8).
        String roleName = "ANALYST".equalsIgnoreCase(request.getRequestedRole()) ? "ANALYST" : "TRADER";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BusinessException("Default role not configured", HttpStatus.INTERNAL_SERVER_ERROR));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .emailVerified(false)
                .active(true)
                .roles(Set.of(role))
                .createdBy("SELF_REGISTRATION")
                .build();

        // userRepository.save(user);
        userRepository.saveAndFlush(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken evt = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        emailVerificationTokenRepository.save(evt);

        emailService.sendVerificationEmail(user.getEmail(), frontendBaseUrl + "/verify-email?token=" + token);
        auditService.log(user.getId(), "REGISTER", "New user registered", null);
    }

    // ------------------------------------------------------------
    // VERIFY EMAIL
    // ------------------------------------------------------------
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken evt = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid verification token", HttpStatus.BAD_REQUEST));

        if (evt.isUsed() || evt.isExpired()) {
            throw new BusinessException("Verification link has expired or already been used", HttpStatus.BAD_REQUEST);
        }

        User user = evt.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        evt.setUsedAt(Instant.now());
        emailVerificationTokenRepository.save(evt);

        auditService.log(user.getId(), "EMAIL_VERIFIED", null, null);
    }

    // ------------------------------------------------------------
    // LOGIN
    // ------------------------------------------------------------
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.isLocked()) {
            throw new BusinessException("Account is locked due to multiple failed login attempts. Reset your password to unlock.", HttpStatus.FORBIDDEN);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            registerFailedAttempt(user);
            auditService.log(user.getId(), "LOGIN_FAILED", "Bad credentials", ipAddress);
            throw ex;
        }

        if (!user.isEmailVerified()) {
            throw new BusinessException("Please verify your email before logging in", HttpStatus.FORBIDDEN);
        }

        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateOpaqueRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshTokenValue))
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()))
                .build();
        refreshTokenRepository.save(refreshToken);

        auditService.log(user.getId(), "LOGIN_SUCCESS", null, ipAddress);

        return buildAuthResponse(user, accessToken, refreshTokenValue);
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLocked(true);
        }
        userRepository.save(user);
    }

    // ------------------------------------------------------------
    // REFRESH TOKEN
    // ------------------------------------------------------------
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hashToken(request.getRefreshToken()))
                .orElseThrow(() -> new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (!stored.isActive()) {
            throw new BusinessException("Refresh token expired or revoked. Please log in again.", HttpStatus.UNAUTHORIZED);
        }

        // rotate: revoke old, issue new
        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshTokenValue = jwtService.generateOpaqueRefreshToken();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(newRefreshTokenValue))
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return buildAuthResponse(user, newAccessToken, newRefreshTokenValue);
    }

    // ------------------------------------------------------------
    // FORGOT / RESET PASSWORD
    // ------------------------------------------------------------
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            PasswordResetToken prt = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                    .build();
            passwordResetTokenRepository.save(prt);
            emailService.sendPasswordResetEmail(user.getEmail(), frontendBaseUrl + "/reset-password?token=" + token);
            auditService.log(user.getId(), "PASSWORD_RESET_REQUESTED", null, null);
        });
        // Intentionally do not reveal whether the email exists (prevents user enumeration).
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessException("Invalid reset token", HttpStatus.BAD_REQUEST));

        if (prt.isUsed() || prt.isExpired()) {
            throw new BusinessException("Reset link has expired or already been used", HttpStatus.BAD_REQUEST);
        }

        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginAttempts(0);
        user.setLocked(false);
        userRepository.save(user);

        prt.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(prt);

        auditService.log(user.getId(), "PASSWORD_RESET_COMPLETED", null, null);
    }

    // ------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    private String hashToken(String rawToken) {
        // Refresh tokens are opaque and stored hashed (never in plaintext)
        // so a DB leak alone can't be used to impersonate a session.
        // SHA-256 is used (not BCrypt) because the hash must be deterministic
        // for exact-match lookup by token_hash.
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
