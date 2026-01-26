package com.example.fourth.service;

import com.example.fourth.dto.ChatGPTRequest;
import com.example.fourth.dto.ChatGPTResponse;
import com.example.fourth.exception.ExternalApiException;
import com.example.fourth.exception.InvalidResponseFormatException;
import com.example.fourth.exception.QuotaExceededException;
import com.example.fourth.exception.RateLimitException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ExternalAiClient {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ExternalAiClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public ChatGPTResponse callChatGpt(ChatGPTRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<ChatGPTRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new RateLimitException("Rate limit exceeded", e);
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN ||
                       e.getStatusCode() == HttpStatus.PAYMENT_REQUIRED) {
                throw new QuotaExceededException("Quota exceeded", e);
            } else {
                throw new ExternalApiException("Error calling external API", e);
            }
        } catch (HttpServerErrorException e) {
            throw new ExternalApiException("Server error from external API", e);
        } catch (Exception e) {
            throw new ExternalApiException("Unexpected error calling external API", e);
        }

        String body = responseEntity.getBody();
        if (body == null || body.isEmpty()) {
            throw new InvalidResponseFormatException("Empty response body from GPT API");
        }
        try {
            return objectMapper.readValue(body, ChatGPTResponse.class);
        } catch (Exception e) {
            throw new InvalidResponseFormatException("Failed to parse GPT API response", e);
        }
    }
}
