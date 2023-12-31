package com.example.springopenai;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class RestEndpoint {

    private final Log log = LogFactory.getLog(RestEndpoint.class);

    private final OpenAIService openAIService;

    public RestEndpoint(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @GetMapping("/")
    public Mono<String> rootEndpoint() {
        try {
            return openAIService.getData();
        } catch (JsonProcessingException e) {
            log.error("Error while processing JSON", e);
            return Mono.empty();
        }
    }
}
