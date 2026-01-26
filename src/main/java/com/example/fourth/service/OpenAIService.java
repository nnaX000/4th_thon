package com.example.fourth.service;

import com.example.fourth.dto.ChatGPTRequest;
import com.example.fourth.dto.ChatGPTResponse;
import com.example.fourth.exception.InvalidResponseFormatException;
import com.example.fourth.exception.QuotaExceededException;
import com.example.fourth.exception.RateLimitException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenAIService {

    private final ExternalAiClient externalAiClient;

    @Value("${openai.api.key}")
    private String key;

    @Value("${openai.api.url}")
    private String apiURL;

    public OpenAIService(ExternalAiClient externalAiClient) {
        this.externalAiClient = externalAiClient;
    }

    public String getTopicFromOpenAI(String prompt) {
        // 프롬프트 길이 제한 (토큰 초과 방지)
        if (prompt.length() > 10000) {
            prompt = prompt.substring(0, 10000);
        }

        ChatGPTRequest request = new ChatGPTRequest("gpt-4-turbo", prompt);

        try {
            ChatGPTResponse response = externalAiClient.callChatGpt(request);
            return response.getChoices().get(0).getMessage().getContent();
        } catch (RateLimitException e) {
            throw e;
        } catch (QuotaExceededException e) {
            throw e;
        } catch (InvalidResponseFormatException e) {
            return "{\"status\":\"AI_RESPONSE_FAILED\"}";
        }
    }


    public ChatGPTResponse convertToChatGPTResponse(String newRecipe) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(newRecipe, ChatGPTResponse.class);
        } catch (Exception e) {
            // 오류 발생 시 예외 처리
            e.printStackTrace();
            return null;
        }
    }
}
