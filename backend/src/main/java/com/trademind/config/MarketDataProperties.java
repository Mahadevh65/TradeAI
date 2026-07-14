package com.trademind.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Config for free-tier market data / news / AI providers.
 * All keys are optional at startup — services degrade gracefully
 * (cached/stale data or a clear "not configured" response) rather
 * than failing the whole application if a key is missing.
 */
@Component
@ConfigurationProperties(prefix = "app.providers")
@Getter
@Setter
public class MarketDataProperties {

    private TwelveData twelveData = new TwelveData();
    private Finnhub finnhub = new Finnhub();
    private NewsApi newsApi = new NewsApi();
    private Ai ai = new Ai();

    @Getter @Setter
    public static class TwelveData {
        private String apiKey;
        private String baseUrl = "https://api.twelvedata.com";
    }

    @Getter @Setter
    public static class Finnhub {
        private String apiKey;
        private String baseUrl = "https://finnhub.io/api/v1";
    }

    @Getter @Setter
    public static class NewsApi {
        private String apiKey;
        private String baseUrl = "https://newsapi.org/v2";
    }

    @Getter @Setter
    public static class Ai {
        // Points at any OpenAI-compatible chat completions endpoint (OpenAI, Anthropic-compatible
        // gateway, local Ollama, etc). Swap base-url/model to change provider without code changes.
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private String model = "gpt-4o-mini";
    }
}
