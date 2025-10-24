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

        // 프롬프트 길이 제한 (토큰 초과 방지)
        if (prompt.length() > 10000) {
            prompt = prompt.substring(0, 10000);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + key);
        headers.set("Content-Type", "application/json");

        ChatGPTRequest request = new ChatGPTRequest("gpt-4-turbo", prompt);

        // JSON 직렬화 (확인용)
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(request);
            // System.out.println("요청 JSON: " + jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{\"error\":\"JSON 직렬화 실패\"}";
        }

        HttpEntity<ChatGPTRequest> entity = new HttpEntity<>(request, headers);

        // 요청 시도 (429 에러시 재시도)
        int retries = 3;
        while (retries-- > 0) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(apiURL, HttpMethod.POST, entity, String.class);
                String body = response.getBody();
                if (body == null || body.isEmpty()) {
                    return "{\"error\":\"빈 응답\"}";
                }
                return body;

            } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                // 429: rate limit 초과 → 대기 후 재시도
                System.out.println("Rate limit 초과. 잠시 대기 후 재시도 중...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            } catch (Exception e) {
                e.printStackTrace();
                return String.format("{\"error\":\"API 요청 실패: %s\"}", e.getMessage());
            }
        }

        return "{\"error\":\"재시도 후에도 실패\"}";
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
