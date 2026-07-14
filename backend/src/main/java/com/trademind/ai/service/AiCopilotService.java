package com.trademind.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trademind.ai.dto.AiQueryRequest;
import com.trademind.ai.dto.AiQueryResponse;
import com.trademind.ai.entity.AiQueryLog;
import com.trademind.ai.repository.AiQueryLogRepository;
import com.trademind.auth.entity.User;
import com.trademind.common.client.AiClient;
import com.trademind.portfolio.dto.PortfolioSummaryResponse;
import com.trademind.portfolio.service.PortfolioService;
import com.trademind.stock.dto.StockDetailsResponse;
import com.trademind.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiCopilotService {

    private final AiClient aiClient;
    private final StockService stockService;
    private final PortfolioService portfolioService;
    private final AiQueryLogRepository queryLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            You are TradeMind AI's Stock Copilot, an equity research assistant embedded in a
            trading dashboard. You answer questions about stocks and portfolios using ONLY the
            context data given to you in the user message — never invent prices or fundamentals
            that weren't provided.

            You MUST reply with a single JSON object and nothing else (no markdown fences, no
            prose outside the JSON), matching exactly this shape:
            {
              "summary": string,
              "riskAnalysis": string,
              "pros": [string, ...],
              "cons": [string, ...],
              "technicalAnalysis": string,
              "fundamentalAnalysis": string,
              "recommendation": "BUY" | "SELL" | "HOLD" | "WATCH",
              "confidenceScore": number (0-100)
            }

            This is not financial advice and you should reflect appropriate uncertainty in your
            confidence score when data is limited.
            """;

    @Transactional
    public AiQueryResponse ask(User user, AiQueryRequest request) {
        if (!aiClient.isConfigured()) {
            return degradedResponse();
        }

        String context = buildContext(user, request.getSymbol());
        String userPrompt = "Context data:\n" + context + "\n\nQuestion: " + request.getQuestion();

        AiQueryResponse response;
        try {
            String rawContent = aiClient.complete(SYSTEM_PROMPT, userPrompt).block();
            response = parseResponse(rawContent);
        } catch (Exception e) {
            log.error("AI Copilot query failed: {}", e.getMessage());
            response = errorResponse();
        }

        AiQueryLog log = AiQueryLog.builder()
                .user(user)
                .question(request.getQuestion())
                .answerSummary(response.getSummary())
                .recommendation(response.getRecommendation())
                .confidenceScore(response.getConfidenceScore())
                .build();
        queryLogRepository.save(log);

        return response;
    }

    private String buildContext(User user, String symbol) {
        StringBuilder sb = new StringBuilder();

        if (symbol != null && !symbol.isBlank()) {
            try {
                StockDetailsResponse stock = stockService.getDetails(symbol);
                sb.append("Stock: ").append(stock.getSymbol()).append(" (").append(stock.getCompanyName()).append(")\n")
                  .append("Sector: ").append(stock.getSector()).append("\n")
                  .append("Last price: ").append(stock.getLastPrice()).append("\n")
                  .append("Day change %: ").append(stock.getDayChangePct()).append("\n")
                  .append("Market cap: ").append(stock.getMarketCap()).append("\n")
                  .append("P/E: ").append(stock.getPeRatio()).append("\n")
                  .append("52w high/low: ").append(stock.getWeek52High()).append(" / ").append(stock.getWeek52Low()).append("\n");
            } catch (Exception e) {
                sb.append("Requested symbol '").append(symbol).append("' could not be resolved.\n");
            }
        }

        try {
            PortfolioSummaryResponse portfolio = portfolioService.getSummary(user.getId());
            sb.append("User portfolio value: ").append(portfolio.getTotalPortfolioValue())
              .append(", total P/L: ").append(portfolio.getTotalProfitLoss())
              .append(", ROI %: ").append(portfolio.getTotalRoiPercent()).append("\n");
            sb.append("Holdings: ");
            portfolio.getHoldings().forEach(h ->
                    sb.append(h.getSymbol()).append(" (qty ").append(h.getQuantity()).append("), "));
        } catch (Exception ignored) {
            // portfolio context is best-effort
        }

        return sb.toString();
    }

    private AiQueryResponse parseResponse(String rawContent) {
        String json = extractJsonBlock(rawContent);
        try {
            var node = objectMapper.readTree(json);
            List<String> pros = toStringList(node.path("pros"));
            List<String> cons = toStringList(node.path("cons"));

            return AiQueryResponse.builder()
                    .summary(node.path("summary").asText(""))
                    .riskAnalysis(node.path("riskAnalysis").asText(""))
                    .pros(pros)
                    .cons(cons)
                    .technicalAnalysis(node.path("technicalAnalysis").asText(""))
                    .fundamentalAnalysis(node.path("fundamentalAnalysis").asText(""))
                    .recommendation(node.path("recommendation").asText("WATCH"))
                    .confidenceScore(BigDecimal.valueOf(node.path("confidenceScore").asDouble(0)))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse AI response as JSON, returning raw text as summary");
            return AiQueryResponse.builder()
                    .summary(rawContent)
                    .riskAnalysis("")
                    .pros(List.of())
                    .cons(List.of())
                    .technicalAnalysis("")
                    .fundamentalAnalysis("")
                    .recommendation("WATCH")
                    .confidenceScore(BigDecimal.ZERO)
                    .build();
        }
    }

    private List<String> toStringList(com.fasterxml.jackson.databind.JsonNode arrayNode) {
        if (!arrayNode.isArray()) return List.of();
        return objectMapper.convertValue(arrayNode, List.class);
    }

    /** Strips markdown code fences in case the model ignores the "no fences" instruction. */
    private String extractJsonBlock(String raw) {
        if (raw == null) return "{}";
        Matcher matcher = Pattern.compile("\\{[\\s\\S]*}").matcher(raw);
        return matcher.find() ? matcher.group() : raw;
    }

    private AiQueryResponse degradedResponse() {
        return AiQueryResponse.builder()
                .summary("AI Copilot isn't configured yet. Add app.providers.ai.api-key " +
                        "(and optionally base-url/model for a non-OpenAI provider) to enable it.")
                .riskAnalysis("").pros(List.of()).cons(List.of())
                .technicalAnalysis("").fundamentalAnalysis("")
                .recommendation("WATCH").confidenceScore(BigDecimal.ZERO)
                .build();
    }

    private AiQueryResponse errorResponse() {
        return AiQueryResponse.builder()
                .summary("The AI Copilot is temporarily unavailable. Please try again shortly.")
                .riskAnalysis("").pros(List.of()).cons(List.of())
                .technicalAnalysis("").fundamentalAnalysis("")
                .recommendation("WATCH").confidenceScore(BigDecimal.ZERO)
                .build();
    }
}
