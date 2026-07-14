package com.trademind.common.util;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around JavaMailSender. Failures are logged rather than
 * thrown so that registration/reset flows never break just because
 * SMTP isn't configured in a local/dev environment.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String verificationLink) {
        send(to, "Verify your TradeMind AI account",
             "Welcome to TradeMind AI. Please verify your email:\n" + verificationLink);
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        send(to, "Reset your TradeMind AI password",
             "You requested a password reset. Use this link (valid 30 minutes):\n" + resetLink);
    }

    public void sendPriceAlertEmail(String to, String symbol, String condition, String targetPrice, String currentPrice) {
        send(to, "Price alert triggered: " + symbol,
             symbol + " has moved " + condition.toLowerCase() + " your target of " + targetPrice +
             " (current price: " + currentPrice + ").");
    }

    private void send(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
