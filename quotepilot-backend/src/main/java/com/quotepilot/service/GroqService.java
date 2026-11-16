
package com.quotepilot.service;

import tools.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GroqService {

    private final WebClient webClient;
    private final String model;

    public GroqService(
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.api.model}") String model) {

        this.model = model;

        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + apiKey.trim()
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    // --------------------------------------------------
    // Connection test
    // --------------------------------------------------

    public void testGroqConnection() {

        log.info("Testing Groq API connectivity...");

        String response = webClient.get()
                .uri("/models")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        if (response == null || response.isBlank()) {
            throw new RuntimeException("Empty response from Groq models API");
        }

        log.info("Groq connection successful!");
        log.info("Groq models response received.");
    }

    // --------------------------------------------------
    // Natural language analyst
    // --------------------------------------------------

    public String generateAnswer(String prompt, String context) {

        String userMessage = """
                Context:
                %s

                Question:
                %s
                """.formatted(
                context == null ? "" : context,
                prompt
        );

        Map<String, Object> request = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                                        You are QuotePilot, an expert procurement analyst.
                                        Be concise, factual and analytical.
                                        Never invent supplier prices, qualifications,
                                        quote values or award decisions.
                                        If information is missing, explicitly say so.
                                        """
                        ),
                        Map.of(
                                "role", "user",
                                "content", userMessage
                        )
                ),
                "temperature", 0.2,
                "max_completion_tokens", 1000
        );

        JsonNode response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(60))
                .block();

        if (response == null || response.path("choices").isEmpty()) {
            throw new RuntimeException("Empty response from Groq");
        }

        return response.path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
    }

    // --------------------------------------------------
    // Structured JSON generation
    // --------------------------------------------------

    public String generateJson(String prompt) {

        Map<String, Object> request = new HashMap<>();

        request.put("model", model);
        request.put("temperature", 0);
        request.put("max_completion_tokens", 4000);

        request.put("messages", List.of(
                Map.of(
                        "role", "system",
                        "content",
                        "Return valid JSON only. Do not include explanations or markdown."
                ),
                Map.of(
                        "role", "user",
                        "content", prompt
                )
        ));

        request.put(
                "response_format",
                Map.of("type", "json_object")
        );

        JsonNode response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(60))
                .block();

        if (response == null || response.path("choices").isEmpty()) {
            throw new RuntimeException("Empty JSON response from Groq");
        }

        return response.path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
    }
}
