package com.example.springopenai;

import com.example.springopenai.data.OpenAIMessage;
import com.example.springopenai.data.OpenAIRequestBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class OpenAIService {

    private final Log log = LogFactory.getLog(OpenAIService.class);

    private final String prompt = """
            Give me a good French recipe for tonight's dinner.
            """;

    @Value("${application.openai.url}")
    private String openAiUrl;

    @Value("${application.openai.key}")
    private String openAiKey;

    private WebClient client;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    @PostConstruct
    public void init() {
        client = WebClient.builder()
                .baseUrl(openAiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + openAiKey)
                .build();
    }

    public Mono<String> getData() throws JsonProcessingException {
        final OpenAIMessage message = OpenAIMessage.builder()
                .role("user")
                .content(prompt)
                .build();

        final OpenAIRequestBody requestBody = OpenAIRequestBody.builder()
                .model("gpt-3.5-turbo")
                .messages(new OpenAIMessage[]{message})
                .build();

        String requestValue = objectMapper.writeValueAsString(requestBody);

        return client.post()
                .uri("/chat/completions")
                .bodyValue(requestValue)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(1))
                                .maxBackoff(Duration.ofSeconds(5))
                );
    }
}
