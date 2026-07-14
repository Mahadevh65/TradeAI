package com.trademind.common.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trademind.config.MarketDataProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Calls any OpenAI-compatible /chat/completions endpoint. Swapping providers
 * (OpenAI, an Anthropic-compatible gateway, a local Ollama instance) is a
 * config change (app.providers.ai.base-url/model/api-key), not a code change.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiClient {

    private final WebClient.Builder webClientBuilder;
    private final MarketDataProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isConfigured() {
        String key = properties.getAi().getApiKey();
        return key != null && !key.isBlank();
    }

    /**
     * Sends a system + user prompt and returns the raw text content of the
     * model's reply. Callers are responsible for parsing structure out of it
     * (we ask the model to reply in strict JSON — see AiCopilotService).
     */
    public Mono<String> complete(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            return Mono.error(new IllegalStateException("AI provider not configured"));
        }

        WebClient client = webClientBuilder
                .baseUrl(properties.getAi().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getAi().getApiKey())
                .build();

        Map<String, Object> body = Map.of(
                "model", properties.getAi().getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.3
        );

        return client.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .map(this::extractContent)
                .onErrorResume(ex -> {
                    log.error("AI completion failed: {}", ex.getMessage());
                    return Mono.error(new IllegalStateException("AI Copilot is temporarily unavailable", ex));
                });
    }

    private String extractContent(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected response from AI provider", e);
        }
    }
}
