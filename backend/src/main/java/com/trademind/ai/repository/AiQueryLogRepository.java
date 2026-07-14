package com.trademind.ai.repository;

import com.trademind.ai.entity.AiQueryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AiQueryLogRepository extends JpaRepository<AiQueryLog, UUID> {
    List<AiQueryLog> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);
}
