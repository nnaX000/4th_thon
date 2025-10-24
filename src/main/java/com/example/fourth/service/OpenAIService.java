package com.example.fourth.service;

import com.example.fourth.dto.ChatGPTRequest;
import com.example.fourth.dto.ChatGPTResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String key;

    @Value("${openai.api.url}")
    private String apiURL;

    public String getTopicFromOpenAI(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + key);
        headers.set("Content-Type", "application/json");

        ChatGPTRequest request = new ChatGPTRequest("gpt-3.5-turbo", prompt);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Error processing JSON";
        }

        HttpEntity<ChatGPTRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(apiURL, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error in API Call";
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
