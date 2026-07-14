package com.trademind.common.util;

import com.trademind.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Lightweight audit logger. Uses JdbcTemplate directly against the
 * audit_logs table (schema.sql) to avoid coupling every module's
 * exception paths to JPA transaction state.
 */
@Service
public class AuditService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(UUID userId, String action, String details, String ipAddress) {
        jdbcTemplate.update(
            "INSERT INTO audit_logs (user_id, action, details, ip_address) VALUES (?, ?, ?, ?)",
            userId, action, details, ipAddress
        );
    }
}
